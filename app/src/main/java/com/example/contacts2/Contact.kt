package com.example.contacts2

data class Contact(
    val id: Long = 0,               // 0 = pas encore enregistré
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String? = null,
    var photoUri: String? = null,   // chemin du fichier dans filesDir
    var latitude: Double? = null,
    var longitude: Double? = null
)