package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.View
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.heightWrapContent
import com.fibelatti.core.extension.onKeyboardSubmit
import com.fibelatti.core.extension.setupLinks
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.LinkTransformationMethod
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.isServerDownException
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.layout_auth_form.*
import javax.inject.Inject

class AuthFragment @Inject constructor() : BaseFragment(R.layout.fragment_auth) {

    private val authViewModel by activityViewModel { viewModelProvider.authViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()

        with(authViewModel) {
            observeEvent(apiTokenError, ::handleAuthError)
            observe(error, ::handleError)
        }
    }

    private fun setupLayout() {
        imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO
        layoutAuthForm.animateChangingTransitions()

        editTextAuthToken.onKeyboardSubmit {
            authViewModel.login(editTextAuthToken.textAsString())
        }
        buttonAuth.setOnClickListener {
            progressBar.visible()
            buttonAuth.gone()
            authViewModel.login(editTextAuthToken.textAsString())
        }

        imageViewAuthHelp.setOnClickListener {
            imageViewAuthHelp.gone()
            textViewAuthHelpTitle.heightWrapContent()
            textViewAuthHelpDescription.heightWrapContent()
            textViewAuthHelpDescription.setupLinks(LinkTransformationMethod())
        }
    }

    private fun handleAuthError(message: String) {
        progressBar.gone()
        buttonAuth.visible()
        textInputLayoutAuthToken.showError(message)
    }

    override fun handleError(error: Throwable) {
        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        progressBar.gone()
        buttonAuth.visible()

        if (error.isServerDownException()) {
            textInputLayoutAuthToken.showError(getString(R.string.server_timeout_error))
        } else {
            activity?.sendErrorReport(error, altMessage = getString(R.string.auth_error))
        }
    }
}
