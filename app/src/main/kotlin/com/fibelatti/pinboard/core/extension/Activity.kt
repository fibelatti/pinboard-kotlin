package com.fibelatti.pinboard.core.extension

import android.app.Activity
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat

fun Activity.shareText(@StringRes title: Int, text: String) {
    ShareCompat.IntentBuilder.from(this)
        .setType("text/plain")
        .setChooserTitle(title)
        .setText(text)
        .startChooser()
}
