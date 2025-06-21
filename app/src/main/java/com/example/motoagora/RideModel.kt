package com.example.motoagora

data class RideModel(
    val id: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val paymentMethod: String = "",
    val value: Double = 7.0,
    var status: String = "pendente",
    val clientPhoneNumber: String = "" // NOVO CAMPO
) {
    constructor() : this("", "", "", "", 7.0, "pendente", "")
}