package com.fibelatti.pinboard.core.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService
import com.fibelatti.pinboard.R

fun Context.copyToClipboard(
    label: String,
    text: String,
) {
    getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(this, R.string.feedback_copied_to_clipboard, Toast.LENGTH_SHORT).show()
}
