package org.zen.fortknox.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.zen.fortknox.database.entity.User

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(user: User)

    @Update
    fun update(user: User)

    /* LIMIT 1 in the end makes sqlite returns only the first matching record */
    @Query("SELECT * FROM Users WHERE username = :username LIMIT 1")
    fun get(username: String): User?

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAll() : LiveData<List<User>>

    @Query("SELECT COUNT(*) FROM Users")
    fun count(): Int

    @Query("SELECT * FROM Users WHERE username LIKE '%' || :query || '%'")
    fun search(query: String): LiveData<List<User>>
}