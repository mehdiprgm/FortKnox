package org.zen.fortknox.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApiUser(
    var id: Int? = null,
    var username: String,
    var password: String,
    var emailAddress: String,
    var securityCode: String,
    var is2FAActivated: Boolean,
    var phoneNumber: String,
    var imagePath: String? = null,
    var loginDateTime: String,
    var isLocked: Boolean,
    var isRoot: Boolean,
    var createDate: String
) : Parcelable