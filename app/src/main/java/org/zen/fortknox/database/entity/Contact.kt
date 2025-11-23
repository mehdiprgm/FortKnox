package org.zen.fortknox.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var userId: Int = 0,
    var name: String,
    var phoneNumber: String,
    var extraInformation: String,
    var createDate: String
) : Parcelable {
    override fun toString(): String {
        return """
            Contact
            --------------------
            Id: $id
            User id: $userId
            
            Name: $name
            Phone number: $phoneNumber
            
            Extra information: $extraInformation
            Create date: $createDate
        """.trimIndent()
    }
}
