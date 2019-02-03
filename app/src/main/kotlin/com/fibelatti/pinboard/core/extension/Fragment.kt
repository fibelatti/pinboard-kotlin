package com.fibelatti.pinboard.core.extension

import androidx.fragment.app.Fragment

fun Fragment.navigateBack() {
    activity?.onBackPressed()
}
