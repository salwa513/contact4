package com.example.contacts2

data class Contact(
    var nom: String,
    var prenom: String,
    var telephone: String,
    var email: String,
    var photo: String = ""
)