package com.example.motoagora

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import retrofit2.Response // <-- IMPORT ADICIONADO AQUI

class DriverMapViewModel : ViewModel() {

    // Agora precisamos do repositório para aceitar a corrida
    private val repository = MotoRepository()

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    private val _earnings = MutableStateFlow(127.50)
    val earnings = _earnings.asStateFlow()

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _recenterMap = MutableStateFlow(0)
    val recenterMap = _recenterMap.asStateFlow()

    private val _newRideAvailable = MutableStateFlow<RideModel?>(null)
    val newRideAvailable = _newRideAvailable.asStateFlow()

    private val _acceptedRide = MutableStateFlow<RideModel?>(null)
    val acceptedRide = _acceptedRide.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private val gson = Gson()

    fun toggleOnlineStatus(context: Context) {
        _isOnline.value = !_isOnline.value
        updateServiceStatus(context)

        if (_isOnline.value) {
            connectAndListenForRides()
        } else {
            disconnectFromSocket()
        }
    }

    private fun connectAndListenForRides() {
        SocketManager.connect()
        Log.d("SocketIO", "Conectando ao socket...")
        SocketManager.on("nova_corrida") { args ->
            Log.d("SocketIO", "Evento 'nova_corrida' recebido!")
            if (args.isNotEmpty() && args[0] is JSONObject) {
                val jsonObject = args[0] as JSONObject
                if (jsonObject.has("id")) {
                    val rideIdInt = jsonObject.getInt("id")
                    jsonObject.put("id", rideIdInt.toString())
                }

                val ride = gson.fromJson(jsonObject.toString(), RideModel::class.java)
                viewModelScope.launch {
                    if (_newRideAvailable.value == null && _acceptedRide.value == null) {
                        _newRideAvailable.value = ride
                        Log.d("SocketIO", "Nova corrida disponível: ${ride.id}")
                    }
                }
            }
        }
    }

    private fun disconnectFromSocket() {
        Log.d("SocketIO", "Desconectando do socket...")
        SocketManager.off("nova_corrida")
        SocketManager.disconnect()
    }

    fun acceptRide(context: Context) {
        val rideToAccept = _newRideAvailable.value ?: return
        val driverId = SessionManager.currentUser?.id ?: return

        _newRideAvailable.value = null

        viewModelScope.launch {
            try {
                Log.d("API_CALL", "Aceitando corrida ${rideToAccept.id} pelo motorista ${driverId}")
                val response: Response<RideModel> = repository.acceptRide(driverId, rideToAccept.id)

                if (response.isSuccessful && response.body() != null) {
                    val acceptedRideData = response.body()!!
                    _acceptedRide.value = acceptedRideData
                    Log.d("API_CALL", "Corrida aceita com sucesso: ${acceptedRideData.id}")
                } else {
                    val errorMsg = "Corrida não mais disponível."
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    Log.e("API_CALL", "Falha ao aceitar corrida: ${response.message()}")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão ao aceitar corrida.", Toast.LENGTH_SHORT).show()
                Log.e("API_CALL", "Exceção ao aceitar corrida", e)
                _newRideAvailable.value = rideToAccept
            }
        }
    }

    fun declineRide() {
        _newRideAvailable.value = null
    }

    fun clearAcceptedRide() {
        _acceptedRide.value = null
    }

    // --- Funções de Serviço e UI (sem grandes mudanças) ---

    fun startServiceIfNeeded(context: Context) {
        updateServiceStatus(context)
    }

    private fun updateServiceStatus(context: Context) {
        val statusText = if (_isOnline.value) "Você está online" else "Você está offline"
        val serviceIntent = Intent(context, DriverForegroundService::class.java).apply {
            action = DriverForegroundService.ACTION_START
            putExtra(DriverForegroundService.EXTRA_STATUS_TEXT, statusText)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun playRideNotificationSound(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.ride_notification)
        }
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun logout(context: Context) {
        disconnectFromSocket()
        context.stopService(Intent(context, DriverForegroundService::class.java))
        SessionManager.clearSession()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _currentLocation.value = GeoPoint(location.latitude, location.longitude)
                }
            }
    }

    fun onCenterMapClick() {
        _recenterMap.value += 1
    }

    override fun onCleared() {
        super.onCleared()
        disconnectFromSocket()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
