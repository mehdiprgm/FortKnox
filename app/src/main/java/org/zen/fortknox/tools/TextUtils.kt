package org.zen.fortknox.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity

fun String.isMultiText() : Boolean {
    return this.contains('\n') || this.contains('\r')
}

fun String.getFirstLine() : String {
    val newLineIndex = this.indexOf('\n')
    val carriageReturnIndex = this.indexOf('\r')

    var endOfFirstLine = -1

    if (newLineIndex != -1 && carriageReturnIndex != -1) {
        endOfFirstLine = minOf(newLineIndex, carriageReturnIndex)
    } else if (newLineIndex != -1) {
        endOfFirstLine = newLineIndex
    } else if (carriageReturnIndex != -1) {
        endOfFirstLine = carriageReturnIndex
    }

    return if (endOfFirstLine != -1) {
        this.substring(0, endOfFirstLine)
    } else {
        this
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text)

    clipboardManager.setPrimaryClip(clipData)
}

fun pasteFromClipboard(context: Context) : String {
    /* Get system clipboard */
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    /* Check if clip exists and there is something in it */
    if (clipboard.hasPrimaryClip() && (clipboard.primaryClip?.itemCount ?: 0) > 0) {
        val data = clipboard.primaryClip!!.getItemAt(0).text ?: return ""
        return data.toString()
    }

    return ""
}

fun shareText(context: Context, title: String, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, title)
    startActivity(context, shareIntent, null)
}

fun maskPassword(string: String) : String {
    val masked = StringBuilder()

    for (i in string.indices) {
        masked.append('*')
    }

    return masked.toString()
}