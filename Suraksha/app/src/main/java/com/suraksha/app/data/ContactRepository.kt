package com.suraksha.app.data

import kotlinx.coroutines.flow.Flow

// The repository provides a clean API to the rest of the app for data access.
// It takes the DAO (Data Access Object) as a parameter.
class ContactRepository(private val contactDao: ContactDao) {

    // This "allContacts" property is a direct feed from the database.
    // When the database updates, this list will automatically update.
    val allContacts: Flow<List<TrustedContact>> = contactDao.getAllContacts()

    // This function lets us add a new contact to the database.
    // "suspend" means it's a background task.
    suspend fun insert(contact: TrustedContact) {
        contactDao.insertContact(contact)
    }

    // This function lets us delete a contact.
    suspend fun delete(contact: TrustedContact) {
        contactDao.deleteContact(contact)
    }
}