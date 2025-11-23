package org.zen.fortknox.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.zen.fortknox.database.entity.Contact

@Dao
interface ContactDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Update
    fun update(contact: Contact)

    /* LIMIT 1 in the end makes sqlite returns only the first matching record */
    @Query("SELECT * FROM Contacts WHERE name = :name LIMIT 1")
    fun get(name: String): Contact?

    @Query("SELECT * FROM Contacts ORDER BY id ASC")
    fun getAll() : LiveData<List<Contact>>

    @Query("SELECT COUNT(*) FROM Contacts")
    fun count(): Int

    @Query("SELECT * FROM Contacts WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): LiveData<List<Contact>>
}