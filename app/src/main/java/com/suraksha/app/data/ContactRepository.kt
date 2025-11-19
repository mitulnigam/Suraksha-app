package com.suraksha.app.data

import kotlinx.coroutines.flow.Flow


class ContactRepository(private val contactDao: ContactDao) {


    val allContacts: Flow<List<TrustedContact>> = contactDao.getAllContacts()


    suspend fun insert(contact: TrustedContact) {
        contactDao.insertContact(contact)
    }

    suspend fun delete(contact: TrustedContact) {
        contactDao.deleteContact(contact)
    }
}