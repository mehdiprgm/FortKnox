package org.zen.fortknox.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory
import org.zen.fortknox.database.dao.AccountDAO
import org.zen.fortknox.database.dao.BankCardDAO
import org.zen.fortknox.database.dao.ContactDAO
import org.zen.fortknox.database.dao.NoteDAO
import org.zen.fortknox.database.dao.UserDAO
import org.zen.fortknox.database.entity.Account
import org.zen.fortknox.database.entity.BankCard
import org.zen.fortknox.database.entity.Contact
import org.zen.fortknox.database.entity.Note
import org.zen.fortknox.database.entity.User
import org.zen.fortknox.database.security.SecureKeyManager
import kotlin.jvm.java

@Database(
    entities = [User::class, Account::class, BankCard::class, Contact::class, Note::class],
    version = 5,
    exportSchema = false
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun accountDAO(): AccountDAO
    abstract fun bankCardDAO(): BankCardDAO
    abstract fun contactDAO(): ContactDAO
    abstract fun noteDAO(): NoteDAO
    abstract fun userDAO(): UserDAO

    companion object {

        @Volatile
        private var INSTANCE: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase {
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, MainDatabase::class.java, "FortKnoxDatabase"
                ).allowMainThreadQueries().fallbackToDestructiveMigration()
                    .openHelperFactory(SupportFactory(SecureKeyManager.getDbKey(context))).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}