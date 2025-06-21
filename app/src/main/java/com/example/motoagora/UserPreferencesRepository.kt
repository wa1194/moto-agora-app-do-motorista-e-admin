package com.example.motoagora

import android.content.Context
import android.content.SharedPreferences

/**
 * Objeto para gerenciar o armazenamento de pequenas preferências do usuário,
 * como as credenciais de login para a função "Lembrar-me".
 *
 * Utiliza SharedPreferences, que é o mecanismo padrão do Android para isso.
 */
object UserPreferencesRepository {
    private const val PREFS_NAME = "motoagora_prefs"
    private const val KEY_LOGIN = "key_login"
    private const val KEY_PASSWORD = "key_password"
    private const val KEY_REMEMBER_ME = "key_remember_me"

    // Retorna a instância do SharedPreferences
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Salva as credenciais se o usuário marcou "Lembrar-me".
     * Nota: Em um app de produção real, a senha NUNCA deve ser salva em texto puro.
     * Considere usar a Keystore do Android para criptografá-la.
     */
    fun saveCredentials(context: Context, login: String, password: String) {
        getPrefs(context).edit()
            .putString(KEY_LOGIN, login)
            .putString(KEY_PASSWORD, password)
            .putBoolean(KEY_REMEMBER_ME, true)
            .apply()
    }

    /**
     * Carrega as credenciais salvas.
     * Retorna um Triple contendo: (login, senha, se "Lembrar-me" estava ativo).
     */
    fun loadCredentials(context: Context): Triple<String, String, Boolean> {
        val prefs = getPrefs(context)
        val login = prefs.getString(KEY_LOGIN, "") ?: ""
        val password = prefs.getString(KEY_PASSWORD, "") ?: ""
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        return Triple(login, password, rememberMe)
    }

    /**
     * Limpa as credenciais salvas se o usuário desmarcar "Lembrar-me" ou fizer logout.
     */
    fun clearCredentials(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_LOGIN)
            .remove(KEY_PASSWORD)
            .putBoolean(KEY_REMEMBER_ME, false)
            .apply()
    }
}
