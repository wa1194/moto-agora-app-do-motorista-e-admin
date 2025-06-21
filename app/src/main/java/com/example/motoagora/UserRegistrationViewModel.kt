package com.example.motoagora

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class UserRegistrationViewModel(
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val repository = MotoRepository()

    val name = mutableStateOf("")
    val age = mutableStateOf("")
    val maritalStatus = mutableStateOf("Solteiro(a)")
    val cpf = mutableStateOf("")
    // val cnh = mutableStateOf("") // Removido
    val phoneNumber = mutableStateOf("") // Adicionado
    val cidade = mutableStateOf("")
    val email = mutableStateOf("")
    val password = mutableStateOf("")

    val cnhPhotoUri = mutableStateOf<Uri?>(null)
    val motoDocUri = mutableStateOf<Uri?>(null)
    val profilePhotoUri = mutableStateOf<Uri?>(null)

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState = _registrationState.asStateFlow()

    fun registerUser() {
        if (listOf(name.value, age.value, cpf.value, cidade.value, email.value, password.value, phoneNumber.value).any { it.isBlank() } || cnhPhotoUri.value == null || motoDocUri.value == null || profilePhotoUri.value == null) {
            _registrationState.value = RegistrationState.Error("Todos os campos e fotos são obrigatórios.")
            return
        }

        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            try {
                val cnhPart = cnhPhotoUri.value!!.toMultipartBodyPart("cnhPhoto")
                val motoDocPart = motoDocUri.value!!.toMultipartBodyPart("motoDoc")
                val profilePhotoPart = profilePhotoUri.value!!.toMultipartBodyPart("profilePhoto")

                val response = repository.registerDriver(
                    name = name.value.toRequestBody(MultipartBody.FORM),
                    age = age.value.toRequestBody(MultipartBody.FORM),
                    maritalStatus = maritalStatus.value.toRequestBody(MultipartBody.FORM),
                    cpf = cpf.value.toRequestBody(MultipartBody.FORM),
                    phoneNumber = phoneNumber.value.toRequestBody(MultipartBody.FORM), // Adicionado
                    cidade = cidade.value.toRequestBody(MultipartBody.FORM),
                    email = email.value.toRequestBody(MultipartBody.FORM),
                    password = password.value.toRequestBody(MultipartBody.FORM),
                    cnhPhoto = cnhPart,
                    motoDoc = motoDocPart,
                    profilePhoto = profilePhotoPart
                )

                if (response.isSuccessful && response.body() != null) {
                    _registrationState.value = RegistrationState.Success(response.body()!!.message)
                    resetForm()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _registrationState.value = RegistrationState.Error("Erro do servidor: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error("Erro de conexão: ${e.message}")
            }
        }
    }

    private fun resetForm() {
        name.value = ""
        age.value = ""
        cpf.value = ""
        phoneNumber.value = ""
        cidade.value = ""
        email.value = ""
        password.value = ""
        cnhPhotoUri.value = null
        motoDocUri.value = null
        profilePhotoUri.value = null
    }

    private fun Uri.toMultipartBodyPart(name: String): MultipartBody.Part {
        val stream = contentResolver.openInputStream(this)!!
        val requestBody = stream.readBytes().toRequestBody(
            contentResolver.getType(this)?.toMediaTypeOrNull()
        )
        stream.close()
        return MultipartBody.Part.createFormData(name, "file.jpg", requestBody)
    }
}