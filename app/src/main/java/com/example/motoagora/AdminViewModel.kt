package com.example.motoagora

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class AdminViewModel : ViewModel() {

    private val repository = MotoRepository()

    private val _pendingUsers = MutableStateFlow<List<UserModel>>(emptyList())
    val pendingUsers = _pendingUsers.asStateFlow()
    private val _approvedUsers = MutableStateFlow<List<UserModel>>(emptyList())
    val approvedUsers = _approvedUsers.asStateFlow()
    private val _rejectedUsers = MutableStateFlow<List<UserModel>>(emptyList())
    val rejectedUsers = _rejectedUsers.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    val showCreateRideDialog = MutableStateFlow(false)
    val startLocationAddress = MutableStateFlow("")

    fun fetchDrivers() {
        viewModelScope.launch {
            _isLoading.value = true
            clearMessages()
            val adminId = SessionManager.currentAdmin?.id
            if (adminId == null) {
                _errorMessage.value = "Sessão de admin inválida."
                _isLoading.value = false
                return@launch
            }

            try {
                val response = repository.getDriversForAdmin(adminId)
                if (response.isSuccessful && response.body() != null) {
                    val allUsers = response.body()!!
                    _pendingUsers.value = allUsers.filter { it.status == "pendente" }
                    _approvedUsers.value = allUsers.filter { it.status == "aprovado" }
                    _rejectedUsers.value = allUsers.filter { it.status == "reprovado" }
                } else {
                    _errorMessage.value = "Erro ao buscar motoristas: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateDriverStatus(driverId: String, newStatus: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val adminId = SessionManager.currentAdmin?.id ?: return@launch
                val response = when (newStatus) {
                    "aprovado" -> repository.approveDriver(adminId, driverId)
                    "reprovado" -> repository.reproveDriver(adminId, driverId)
                    else -> return@launch
                }
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _errorMessage.value = "Falha ao atualizar status: ${response.message()}"
                }
            } catch(e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
            }
        }
    }

    fun approveDriver(driverId: String) {
        updateDriverStatus(driverId, "aprovado") { fetchDrivers() }
    }

    fun reproveDriver(driverId: String) {
        updateDriverStatus(driverId, "reprovado") { fetchDrivers() }
    }

    fun createRide(start: String, end: String, payment: String) {
        if (start.isBlank() || end.isBlank()) {
            _errorMessage.value = "Preencha o local de partida e o destino."
            return
        }
        viewModelScope.launch {
            try {
                val adminId = SessionManager.currentAdmin?.id ?: return@launch
                val ride = RideModel(startLocation = start, endLocation = end, paymentMethod = payment)
                val response = repository.createRide(adminId, ride)
                if(response.isSuccessful) {
                    _successMessage.value = "Corrida criada com sucesso!"
                    showCreateRideDialog.value = false
                } else {
                    _errorMessage.value = "Erro do servidor ao criar corrida."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao criar corrida: ${e.message}"
            }
        }
    }

    fun stopSimulatedRides() {
        viewModelScope.launch {
            try {
                val adminId = SessionManager.currentAdmin?.id ?: return@launch
                val response = repository.stopAllSimulations(adminId)
                if (response.isSuccessful) {
                    _successMessage.value = response.body()?.message ?: "Corridas pendentes removidas."
                } else {
                    _errorMessage.value = "Falha ao parar corridas."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.message}"
            }
        }
    }

    fun logout() {
        SessionManager.clearSession()
    }

    @SuppressLint("MissingPermission")
    fun onOpenCreateRideDialog(context: Context) {
        showCreateRideDialog.value = true
        viewModelScope.launch {
            startLocationAddress.value = "Obtendo localização..."
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                        startLocationAddress.value = addresses.firstOrNull()?.getAddressLine(0) ?: "Endereço não encontrado"
                                    }
                                } else {
                                    @Suppress("DEPRECATION")
                                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    startLocationAddress.value = addresses?.firstOrNull()?.getAddressLine(0) ?: "Endereço não encontrado"
                                }
                            } catch (e: Exception) {
                                startLocationAddress.value = "Erro ao obter endereço"
                            }
                        }
                    } else {
                        startLocationAddress.value = "Não foi possível obter a localização"
                    }
                }
                .addOnFailureListener {
                    startLocationAddress.value = "Falha ao obter localização"
                }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}