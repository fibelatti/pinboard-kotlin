package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.getViewToApplyInsets
import com.fibelatti.pinboard.core.di.ViewModelProvider

abstract class BaseFragment : Fragment() {

    protected val viewModelProvider: ViewModelProvider
        get() = (activity as BaseActivity).viewModelProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getViewToApplyInsets(view).doOnApplyWindowInsets { viewToApply, insets, initialPadding, _ ->
            ViewCompat.setPaddingRelative(
                viewToApply,
                initialPadding.start,
                initialPadding.top,
                initialPadding.end,
                initialPadding.bottom + insets.systemWindowInsetBottom
            )
        }
    }

    open fun handleError(error: Throwable) {
        (activity as BaseActivity).handleError(error)
    }
}
