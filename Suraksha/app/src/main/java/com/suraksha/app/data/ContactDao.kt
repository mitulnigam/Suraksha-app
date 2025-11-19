package com.suraksha.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    // Inserts a new contact. If a contact with the same ID exists, it replaces it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContact)

    // Deletes a contact.
    @Delete
    suspend fun deleteContact(contact: TrustedContact)

    // Gets all contacts from the table and orders them by name.
    // The "Flow" makes this list automatically update when the data changes.
    @Query("SELECT * FROM trusted_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<TrustedContact>>
}