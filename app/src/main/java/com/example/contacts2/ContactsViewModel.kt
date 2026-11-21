package com.example.contacts2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contacts2.data.ContactDao
import com.example.contacts2.data.ContactDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = ContactDao(ContactDbHelper.getInstance(app))

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _contacts.value = withContext(Dispatchers.IO) { dao.getAll() }
        }
    }

    suspend fun getById(id: Long): Contact? =
        withContext(Dispatchers.IO) { dao.getById(id) }

    fun save(c: Contact, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                if (c.id == 0L) dao.insert(c) != -1L else dao.update(c) > 0
            }
            if (ok) charger()
            onDone(ok)
        }
    }

    fun delete(id: Long, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { dao.delete(id) > 0 }
            if (ok) charger()
            onDone(ok)
        }
    }
}