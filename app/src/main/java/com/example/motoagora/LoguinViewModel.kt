package com.example.motoagora

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// A classe LoginState permanece a mesma
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class UserSuccess(val user: UserModel) : LoginState()
    data class AdminSuccess(val admin: AdminModel) : LoginState()
    data class Error(val message: String) : LoginState()
}
// A classe GenericLoginResponse permanece a mesma
data class GenericLoginResponse(
    val type: String,
    val user: JsonElement
)

class LoginViewModel : ViewModel() {
    private val repository = MotoRepository()
    private val gson = Gson()

    val loginIdentifier = mutableStateOf("")
    val password = mutableStateOf("")
    val rememberMe = mutableStateOf(false)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun loadSavedCredentials(context: Context) {
        viewModelScope.launch {
            val (savedLogin, savedPassword, wasRemembered) = UserPreferencesRepository.loadCredentials(context)
            loginIdentifier.value = savedLogin
            password.value = savedPassword
            rememberMe.value = wasRemembered
        }
    }

    fun handleRememberMe(context: Context) {
        if (rememberMe.value) {
            UserPreferencesRepository.saveCredentials(context, loginIdentifier.value, password.value)
        } else {
            UserPreferencesRepository.clearCredentials(context)
        }
    }

    fun performLogin() {
        if (loginIdentifier.value.isBlank() || password.value.isBlank()) {
            _loginState.value = LoginState.Error("Preencha todos os campos.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val request = LoginRequest(login = loginIdentifier.value, password = password.value)
                val response = repository.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val jsonElement = response.body()!!
                    try {
                        val loginResponse = gson.fromJson(jsonElement, GenericLoginResponse::class.java)
                        when (loginResponse.type) {
                            "admin" -> {
                                val adminModel = gson.fromJson(loginResponse.user, AdminModel::class.java)
                                SessionManager.currentAdmin = adminModel
                                _loginState.value = LoginState.AdminSuccess(adminModel)
                            }
                            "driver" -> {
                                val userModel = gson.fromJson(loginResponse.user, UserModel::class.java)
                                SessionManager.currentUser = userModel
                                _loginState.value = LoginState.UserSuccess(userModel)
                            }
                            else -> {
                                _loginState.value = LoginState.Error("Tipo de usuário desconhecido.")
                            }
                        }
                    } catch (e: JsonSyntaxException) {
                        _loginState.value = LoginState.Error("Erro ao processar resposta do servidor.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Usuário ou senha inválidos"
                    _loginState.value = LoginState.Error("Falha no login: $errorBody")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Erro de conexão: ${e.message}")
            }
        }
    }
}
