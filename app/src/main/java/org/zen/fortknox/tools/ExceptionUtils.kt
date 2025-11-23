package org.zen.fortknox.tools

import java.io.PrintWriter
import java.io.StringWriter

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class UserNotFoundException(message: String) : AppException(message)
    class UserConflictException(message: String) : AppException(message)
    class NetworkException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class ApiException(message: String, val code: Int) : AppException(message)
    class UnexpectedException(message: String, cause: Throwable? = null) :
        AppException(message, cause)
}

fun Throwable.getStackTrace(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)

    this.printStackTrace(pw)
    return sw.toString()
}