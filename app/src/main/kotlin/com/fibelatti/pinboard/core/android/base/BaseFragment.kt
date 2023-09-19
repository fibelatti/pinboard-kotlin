package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(inflater.context)

    open fun handleError(error: Throwable?, postAction: () -> Unit = {}) {
        (activity as BaseActivity).handleError(error, postAction)
    }
}
