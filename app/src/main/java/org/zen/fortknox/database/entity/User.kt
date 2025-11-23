package org.zen.fortknox.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Users")
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var username: String,
    var password: String,
    var emailAddress: String,
    var securityCode: String,
    var is2FAActivated: Boolean,
    var phoneNumber: String,
    var imagePath: String,
    var loginDateTime: String,
    var isLocked: Boolean,
    var isRoot: Boolean,
    var createDate: String
)  : Parcelable