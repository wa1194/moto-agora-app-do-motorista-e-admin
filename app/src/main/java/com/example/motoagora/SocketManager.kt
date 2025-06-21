package com.example.motoagora

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {

    // Nossa instância única do Socket
    private var mSocket: Socket? = null

    // URL do seu servidor no Render
    // Lembre-se que já adicionamos a dependência no build.gradle
    private const val SERVER_URL = "https://servidor-moto-app.onrender.com/"

    // Garante que teremos apenas uma instância do socket (padrão Singleton)
    @Synchronized
    fun getSocket(): Socket {
        if (mSocket == null) {
            try {
                // Cria a conexão com o servidor
                mSocket = IO.socket(SERVER_URL)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
                throw RuntimeException("A URL do servidor é inválida: $SERVER_URL")
            }
        }
        return mSocket!!
    }

    // Função para estabelecer a conexão
    @Synchronized
    fun connect() {
        if (!getSocket().connected()) {
            getSocket().connect()
        }
    }

    // Função para desconectar
    @Synchronized
    fun disconnect() {
        if (getSocket().connected()) {
            getSocket().disconnect()
        }
    }

    // Função genérica para ouvir qualquer evento vindo do servidor
    fun on(eventName: String, listener: (Array<Any>) -> Unit) {
        getSocket().on(eventName, listener)
    }

    // Função genérica para parar de ouvir um evento
    fun off(eventName: String) {
        getSocket().off(eventName)
    }
}
