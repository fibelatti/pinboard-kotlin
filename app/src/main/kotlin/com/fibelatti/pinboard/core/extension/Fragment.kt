package com.fibelatti.pinboard.core.extension

import androidx.fragment.app.Fragment
import com.fibelatti.core.extension.orFalse

fun Fragment.navigateBack() {
    activity?.onBackPressed()
}

fun Fragment.isAtTheTop(): Boolean = activity?.isFragmentAtTheTop(this).orFalse()
