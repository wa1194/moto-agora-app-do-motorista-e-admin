package com.example.motoagora

/**
 * Objeto singleton para guardar o estado da sessão do usuário logado.
 * Em um app real, use uma solução mais robusta como DataStore.
 * Para este exemplo, isso é suficiente.
 */
object SessionManager {
    var currentAdmin: AdminModel? = null
    var currentUser: UserModel? = null

    fun clearSession() {
        currentAdmin = null
        currentUser = null
    }
}
