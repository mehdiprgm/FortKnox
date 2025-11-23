package org.zen.fortknox.tools

import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.database.entity.User

fun ApiUser.toDatabaseUser(): User {
    return User(
        id = this.id ?: 0,
        username = this.username,
        password = this.password,
        emailAddress = this.emailAddress,
        securityCode = this.securityCode,
        is2FAActivated = this.is2FAActivated,
        phoneNumber = this.phoneNumber,
        imagePath = this.imagePath ?: "",
        loginDateTime = this.loginDateTime,
        isLocked = this.isLocked,
        isRoot = this.isRoot,
        createDate = this.createDate
    )
}

fun User.toApiUser(): ApiUser {
    return ApiUser(
        id = this.id,
        username = this.username,
        password = this.password,
        emailAddress = this.emailAddress,
        securityCode = this.securityCode,
        is2FAActivated = this.is2FAActivated,
        phoneNumber = this.phoneNumber,
        imagePath = this.imagePath ?: "",
        loginDateTime = this.loginDateTime,
        isLocked = this.isLocked,
        isRoot = this.isRoot,
        createDate = this.createDate
    )
}