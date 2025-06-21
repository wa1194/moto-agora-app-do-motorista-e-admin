package com.example.motoagora

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val maritalStatus: String = "",
    val cpf: String = "",
    // val cnh: String = "", // Removido
    val phoneNumber: String = "", // Adicionado
    val cnhPhotoUrl: String = "",
    val motoDocUrl: String = "",
    val profilePhotoUrl: String = "",
    val status: String = "", // "pendente", "aprovado", "reprovado"
    val cidade: String = ""
) {
    // Construtor vazio necessário para o Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "")
}