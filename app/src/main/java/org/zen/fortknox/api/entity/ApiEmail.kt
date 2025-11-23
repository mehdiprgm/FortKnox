package org.zen.fortknox.api.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApiEmail(
    var timeDate: String,
    var emailAddress: String,
    var code: String
) : Parcelable