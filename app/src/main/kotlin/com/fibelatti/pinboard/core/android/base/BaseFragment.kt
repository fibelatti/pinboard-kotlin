package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.view.View
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.doOnApplyWindowInsets
import com.fibelatti.pinboard.core.extension.getViewToApplyInsets
import com.fibelatti.pinboard.core.extension.toast

abstract class BaseFragment : Fragment {

    protected val viewModelFactory
        get() = (activity as BaseActivity).viewModelFactory

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getViewToApplyInsets(view)?.doOnApplyWindowInsets { viewToApply, insets, initialPadding, _ ->
            viewToApply.setPadding(
                initialPadding.left,
                initialPadding.top,
                initialPadding.right,
                initialPadding.bottom + insets.systemWindowInsetBottom
            )
        }
    }

    open fun handleError(error: Throwable) {
        activity?.toast(getString(R.string.generic_msg_error))
        if (BuildConfig.DEBUG) error.printStackTrace()
    }
}
