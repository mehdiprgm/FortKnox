package org.zen.fortknox.tools

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private fun prepareFilePart(file: File, partName: String): MultipartBody.Part {
    // تعیین نوع مدیا (MediaType) بر اساس پسوند فایل یا نوع آن
    val mediaType = "image/*".toMediaTypeOrNull() // باید دقیق‌تر باشد، مثلاً image/jpeg.
    val requestBody = file.asRequestBody(mediaType)

    // ساخت Part اصلی با کلید "file" (که Flask انتظارش را دارد)
    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
}