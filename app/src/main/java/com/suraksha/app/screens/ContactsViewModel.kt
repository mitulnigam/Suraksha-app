package com.suraksha.app.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.suraksha.app.data.AppDatabase
import com.suraksha.app.data.ContactRepository
import com.suraksha.app.data.TrustedContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val allContacts: Flow<List<TrustedContact>>

    init {
        val contactDao = AppDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        allContacts = repository.allContacts
    }

    fun addContact(name: String, phone: String) = viewModelScope.launch {
        val newContact = TrustedContact(name = name, phoneNumber = phone)
        repository.insert(newContact)
    }

    fun deleteContact(contact: TrustedContact) = viewModelScope.launch {
        repository.delete(contact)
    }
}
