package com.example.contacts2

data class Contact(
    val id: Int,
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photoUri: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)