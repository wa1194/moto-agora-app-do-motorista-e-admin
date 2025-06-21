package com.example.motoagora

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // !!! IMPORTANTE !!!
    // Endereço IP do seu computador na rede local.
    private const val BASE_URL = "https://servidor-moto-app.onrender.com/"

    // Intercetador para vermos os logs das chamadas de rede no Logcat (ótimo para depurar)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Cria a instância do Retrofit que será usada em todo o app
    val api: MotoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MotoApiService::class.java)
    }
}
