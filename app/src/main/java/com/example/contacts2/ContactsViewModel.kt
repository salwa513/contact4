package com.example.contacts2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    private val data = mutableListOf(
        Contact(1, "Ben Ali", "Ahmed", "20111222", "ahmed@mail.com", null, 33.8815, 10.0982),
        Contact(2, "Trabelsi", "Sarra", "22333444", "sarra@mail.com", null, 36.8065, 10.1815),
        Contact(3, "Mansouri", "Mohamed", "55444555", "mohamed@mail.com", null, null, null),
        Contact(4, "Hamdi", "Amira", "99111222", "amira@mail.com", null, 35.8256, 10.6084)
    )

    private val _contacts = MutableLiveData<List<Contact>>(data.toList())
    val contacts: LiveData<List<Contact>> = _contacts

    fun getById(id: Int): Contact? = data.find { it.id == id }

    fun update(c: Contact) {
        val i = data.indexOfFirst { it.id == c.id }
        if (i >= 0) {
            data[i] = c
            _contacts.value = data.toList()
        }
    }
}