package org.zen.fortknox.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "BankCards")
data class BankCard(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var userId: Int = 0,
    var cardName: String,
    var cardNumber: String,
    var cvv2: String,
    var expireDate: String,
    var password: String,
    var createDate: String
) : Parcelable {
    override fun toString(): String {
        return """
            Bank Card
            --------------------
            Id: $id
            User id: $userId
            
            Card name: $cardName
            Card number: $cardNumber
            Cvv2: $cvv2
            
            Expire date: $expireDate
            Password: $password
            Create date: $createDate
        """.trimIndent()
    }
}
