package org.zen.fortknox.api.base.response

data class ApiResponse<T>(
    val message: String? = null,
    val status: Int? = null,
    val data: T? = null
)