package org.zen.fortknox.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.zen.fortknox.database.entity.Note

@Dao
interface NoteDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(note: Note)

    @Delete
    fun delete(note: Note)

    @Update
    fun update(note: Note)

    /* LIMIT 1 in the end makes sqlite returns only the first matching record */
    @Query("SELECT * FROM Notes WHERE name = :name LIMIT 1")
    fun get(name: String): Note?

    @Query("SELECT * FROM Notes ORDER BY id ASC")
    fun getAll() : LiveData<List<Note>>

    @Query("SELECT COUNT(*) FROM Notes")
    fun count(): Int

    @Query("SELECT * FROM Notes WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): LiveData<List<Note>>
}