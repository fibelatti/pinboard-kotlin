package com.fibelatti.pinboard.core.android.base

import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.toast

abstract class BaseFragment : Fragment() {

    protected val viewModelFactory
        get() = (activity as BaseActivity).viewModelFactory

    open fun handleError(error: Throwable) {
        activity?.toast(getString(R.string.generic_msg_error))
        if (BuildConfig.DEBUG) error.printStackTrace()
    }
}
