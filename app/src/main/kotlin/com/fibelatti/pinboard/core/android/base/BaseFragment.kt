package com.fibelatti.pinboard.core.android.base

import androidx.fragment.app.Fragment

abstract class BaseFragment :
    Fragment() {

    protected val injector by lazy { (activity as BaseActivity).injector }
    protected val viewModelFactory by lazy { (activity as BaseActivity).viewModelFactory }

    open fun handleError(error: Throwable) {
        error.printStackTrace()
    }
}
