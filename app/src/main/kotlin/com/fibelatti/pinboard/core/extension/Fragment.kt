package com.fibelatti.pinboard.core.extension

import androidx.fragment.app.Fragment
import com.fibelatti.core.extension.orFalse

// TODO - Move to CoreLib
fun Fragment.navigateBack() {
    activity?.onBackPressed()
}

// TODO - Move to CoreLib
fun Fragment.isAtTheTop(): Boolean = activity?.isFragmentAtTheTop(this).orFalse()
