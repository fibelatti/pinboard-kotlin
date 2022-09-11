package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.heightWrapContent
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.core.extension.setupLinks
import com.fibelatti.core.extension.showError
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.LinkTransformationMethod
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "AuthFragment"
    }

    private val authViewModel: AuthViewModel by viewModels()

    private val binding by viewBinding(FragmentAuthBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentAuthBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()

        authViewModel.apiTokenError
            .onEach(::handleAuthError)
            .launchInAndFlowWith(viewLifecycleOwner)
        authViewModel.error
            .onEach(::handleError)
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun setupLayout() {
        with(binding.layoutAuthForm) {
            editTextAuthToken.onActionOrKeyboardSubmit(EditorInfo.IME_ACTION_GO) {
                authViewModel.login(textAsString())
            }
            buttonAuth.setOnClickListener {
                progressBar.isVisible = true
                buttonAuth.isGone = true
                authViewModel.login(editTextAuthToken.textAsString())
            }

            imageViewAuthHelp.setOnClickListener {
                imageViewAuthHelp.isGone = true
                textViewAuthHelpTitle.heightWrapContent()
                textViewAuthHelpDescription.heightWrapContent()
                textViewAuthHelpDescription.setupLinks(LinkTransformationMethod())
            }
        }

        activity?.reportFullyDrawn()
    }

    private fun handleAuthError(message: String) {
        with(binding.layoutAuthForm) {
            buttonAuth.isVisible = true
            progressBar.isGone = true
            textInputLayoutAuthToken.showError(message)
        }
    }

    override fun handleError(error: Throwable) {
        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        with(binding.layoutAuthForm) {
            progressBar.isGone = true
            buttonAuth.isVisible = true
        }

        if (error.isServerException()) {
            binding.layoutAuthForm.textInputLayoutAuthToken.showError(getString(R.string.server_timeout_error))
        } else {
            activity?.sendErrorReport(error, altMessage = getString(R.string.auth_error))
        }
    }
}
