package com.fibelatti.pinboard.features.splash.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import javax.inject.Inject

class SplashFragment @Inject constructor() : BaseFragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO
    }
}
