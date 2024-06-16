package com.fibelatti.core.android.extension

import androidx.fragment.app.Fragment

/**
 * Shorthand function to invoke `onBackPressed` of the parent activity.
 */
fun Fragment.navigateBack() {
    activity?.onBackPressedDispatcher?.onBackPressed()
}
