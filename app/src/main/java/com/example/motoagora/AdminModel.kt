package com.example.motoagora

import com.google.firebase.firestore.IgnoreExtraProperties

// Anotação importante para o Firestore ignorar campos extras
@IgnoreExtraProperties
data class AdminModel(
    val id: String = "",
    val email: String = "",
    val role: String = "", // "master" ou "cidade"
    val managedCities: List<String> = emptyList()
) {
    // Um construtor vazio é necessário para o Firestore converter o documento em objeto
    constructor() : this("", "", "", emptyList())
}
