package org.zen.fortknox.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var userId: Int = 0,
    var name: String,
    var username: String,
    var password: String,
    var accountType: String,
    var extraInformation: String,
    var createDate: String
) : Parcelable {
    override fun toString(): String {
        return """
            Account
            --------------------
            Id: $id
            User id: $userId
            
            Name: $name
            Username: $username
            Password: $password
            
            Account type: $accountType
            Extra information: $extraInformation
            Create date: $createDate
        """.trimIndent()
    }
}