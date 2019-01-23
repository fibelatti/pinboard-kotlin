package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionInflater
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.heightWrapContent
import com.fibelatti.core.extension.isKeyboardSubmit
import com.fibelatti.core.extension.setupLinks
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.textAsString
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.LinkTransformationMethod
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.android.synthetic.main.layout_auth_form.*
import javax.inject.Inject

class AuthFragment @Inject constructor() : BaseFragment() {

    private val authViewModel: AuthViewModel by lazy {
        viewModelFactory.get<AuthViewModel>(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)

        observeEvent(authViewModel.apiTokenError, ::handleAuthError)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
    }

    private fun setupLayout() {
        imageViewAppLogo.transitionName = SharedElementTransitionNames.APP_LOGO
        layoutAuthForm.animateChangingTransitions()

        editTextAuthToken.setOnEditorActionListener { _, actionId, event ->
            if (isKeyboardSubmit(actionId, event)) authViewModel.login(editTextAuthToken.textAsString())
            return@setOnEditorActionListener true
        }
        buttonAuth.setOnClickListener { authViewModel.login(editTextAuthToken.textAsString()) }

        imageViewAuthHelp.setOnClickListener {
            imageViewAuthHelp.gone()
            textViewAuthHelpTitle.heightWrapContent()
            textViewAuthHelpDescription.heightWrapContent()
            textViewAuthHelpDescription.setupLinks(LinkTransformationMethod())
        }
    }

    private fun handleAuthError(message: String) {
        textInputLayoutAuthToken.showError(message)
    }
}
