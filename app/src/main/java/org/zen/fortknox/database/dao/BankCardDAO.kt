package org.zen.fortknox.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.zen.fortknox.database.entity.BankCard

@Dao
interface BankCardDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(bankCard: BankCard)

    @Delete
    fun delete(bankCard: BankCard)

    @Update
    fun update(bankCard: BankCard)

    /* LIMIT 1 in the end makes sqlite returns only the first matching record */
    @Query("SELECT * FROM BankCards WHERE cardName = :cardName LIMIT 1")
    fun get(cardName: String): BankCard?

    @Query("SELECT * FROM BankCards ORDER BY id ASC")
    fun getAll() : LiveData<List<BankCard>>

    @Query("SELECT COUNT(*) FROM BankCards")
    fun count(): Int

    @Query("SELECT * FROM BankCards WHERE cardName LIKE '%' || :query || '%'")
    fun search(query: String): LiveData<List<BankCard>>
}