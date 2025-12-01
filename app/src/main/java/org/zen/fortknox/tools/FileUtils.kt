package org.zen.fortknox.tools

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.copyTo

fun uriToFile(context: Context, uri: Uri, fileName: String? = null): File? {
    val contentResolver = context.contentResolver
    val inputStream: InputStream? = try {
        contentResolver.openInputStream(uri)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    if (inputStream == null) {
        return null
    }

    val tempFileName = fileName ?: getFileNameFromUri(contentResolver, uri) ?: "temp_upload_file"

    val tempFile = File(context.cacheDir, tempFileName)
    try {
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use {
                it.copyTo(outputStream)
            }
        }

        return tempFile
    } catch (e: IOException) {
        e.printStackTrace()
        tempFile.delete()
        return null
    }
}

private fun getFileNameFromUri(contentResolver: android.content.ContentResolver, uri: Uri): String? {
    var name: String? = null
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
    }
    return name
}

/* Create a temporary file to store the camera image */
fun createImageFile(context: Context): File {
    // Create unique filename using timestamp
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "JPEG_${timeStamp}_"

    // Get the Pictures directory
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    // Create temporary file
    return File.createTempFile(
        fileName,  // prefix
        ".jpg",    // suffix
        storageDir // directory
    )
}