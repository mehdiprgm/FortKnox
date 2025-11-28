package org.zen.fortknox.tools

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private fun prepareFilePart(file: File, partName: String): MultipartBody.Part {
    val mediaType = "image/*".toMediaTypeOrNull()
    val requestBody = file.asRequestBody(mediaType)

    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
}