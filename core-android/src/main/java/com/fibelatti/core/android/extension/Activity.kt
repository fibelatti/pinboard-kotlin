package com.fibelatti.core.android.extension

import androidx.appcompat.app.AppCompatActivity

/**
 * Shorthand function to invoke the `onBackPressed` of this activity's `onBackPressedDispatcher`.
 */
public fun AppCompatActivity.navigateBack() {
    onBackPressedDispatcher.onBackPressed()
}
