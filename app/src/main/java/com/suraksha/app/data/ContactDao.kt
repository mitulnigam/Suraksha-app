package com.suraksha.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContact)

    @Delete
    suspend fun deleteContact(contact: TrustedContact)


    @Query("SELECT * FROM trusted_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<TrustedContact>>
}