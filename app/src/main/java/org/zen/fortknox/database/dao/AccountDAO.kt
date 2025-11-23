package org.zen.fortknox.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.zen.fortknox.database.entity.Account

@Dao
interface AccountDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(account: Account)

    @Delete
    fun delete(account: Account)

    @Update
    fun update(account: Account)

    /* LIMIT 1 in the end makes sqlite returns only the first matching record */
    @Query("SELECT * FROM Accounts WHERE name = :name LIMIT 1")
    fun get(name: String): Account?

    @Query("SELECT * FROM Accounts ORDER BY id ASC")
    fun getAll() : LiveData<List<Account>>

    @Query("SELECT COUNT(*) FROM Accounts")
    fun count(): Int

    @Query("SELECT * FROM Accounts WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): LiveData<List<Account>>
}