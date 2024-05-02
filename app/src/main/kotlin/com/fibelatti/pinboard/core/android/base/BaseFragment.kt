package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.fibelatti.core.android.fragmentArgs
import com.fibelatti.pinboard.R

abstract class BaseFragment : Fragment() {

    var enterTransitionRes: Int? by fragmentArgs()

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.fade)
        enterTransition = inflater.inflateTransition(enterTransitionRes ?: R.transition.fade)
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(inflater.context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    open fun handleError(error: Throwable?, postAction: () -> Unit = {}) {
        (activity as BaseActivity).handleError(error, postAction)
    }
}
