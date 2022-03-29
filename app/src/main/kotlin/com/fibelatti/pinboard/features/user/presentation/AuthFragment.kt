package com.fibelatti.pinboard.features.user.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "AuthFragment"
    }

    private val authViewModel: AuthViewModel by activityViewModels()

    private val binding by viewBinding(FragmentAuthBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentAuthBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()

        lifecycleScope.launch {
            authViewModel.apiTokenError.collect(::handleAuthError)
        }
        lifecycleScope.launch {
            authViewModel.error.collect(::handleError)
        }
    }

    private fun setupLayout() {
        with(binding.layoutAuthForm) {
            root.animateChangingTransitions()

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
    }

    private fun handleAuthError(message: String) {
        with(binding.layoutAuthForm) {
            buttonAuth.visible()
            progressBar.gone()
            textInputLayoutAuthToken.showError(message)
        }
    }

    override fun handleError(error: Throwable) {
        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        with(binding.layoutAuthForm) {
            progressBar.gone()
            buttonAuth.visible()
        }

        if (error.isServerException()) {
            binding.layoutAuthForm.textInputLayoutAuthToken.showError(getString(R.string.server_timeout_error))
        } else {
            activity?.sendErrorReport(error, altMessage = getString(R.string.auth_error))
        }
    }
}
