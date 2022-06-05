package com.fibelatti.core.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Shorthand function to invoke [FragmentActivity.onBackPressed] of the parent [FragmentActivity].
 */
fun Fragment.navigateBack() {
    activity?.onBackPressed()
}
