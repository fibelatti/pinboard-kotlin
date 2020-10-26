package com.fibelatti.pinboard.features.splash.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentSplashBinding
import javax.inject.Inject

class SplashFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "SplashFragment"
    }

    private var binding by viewBinding<FragmentSplashBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentSplashBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO
    }
}
