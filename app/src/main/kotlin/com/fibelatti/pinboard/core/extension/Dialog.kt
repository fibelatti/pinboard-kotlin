package com.fibelatti.pinboard.core.extension

import android.content.Context
import androidx.core.content.ContextCompat
import com.fibelatti.pinboard.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.showStyledDialog(configuration: MaterialAlertDialogBuilder.() -> Unit) {
    MaterialAlertDialogBuilder(this, R.style.AppTheme_AlertDialog)
        .setBackground(ContextCompat.getDrawable(this, R.drawable.background_contrast_rounded))
        .apply(configuration)
        .show()
}
