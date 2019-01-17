package com.fibelatti.pinboard.features.splash.presentation

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.inTransaction
import com.fibelatti.pinboard.features.auth.presentation.AuthFragment
import kotlinx.android.synthetic.main.fragment_splash.*

class SplashFragment : BaseFragment() {
    companion object {
        @JvmStatic
        val TAG: String = SplashFragment::class.java.simpleName

        fun newInstance(): SplashFragment = SplashFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_splash, container, false)

    @Suppress("MagicNumber")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler().postDelayed({

            imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO

            inTransaction {
                replace(R.id.fragmentHost, AuthFragment.newInstance())
                    .addSharedElement(imageViewAppLogo, SharedElementTransitionNames.APP_LOGO)
            }
        }, 1000L)
    }
}
