package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.getViewToApplyInsets

abstract class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getViewToApplyInsets(view).doOnApplyWindowInsets { viewToApply, insets, initialPadding, _ ->
            viewToApply.updatePadding(
                bottom = initialPadding.bottom + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            )
        }
    }

    open fun handleError(error: Throwable?, postAction: () -> Unit = {}) {
        (activity as BaseActivity).handleError(error, postAction)
    }
}
