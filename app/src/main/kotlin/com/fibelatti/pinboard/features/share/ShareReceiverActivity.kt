package com.fibelatti.pinboard.features.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.showStyledDialog
import com.fibelatti.core.extension.toast
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.ActivityShareBinding
import com.fibelatti.pinboard.features.MainActivity
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection

@AndroidEntryPoint
class ShareReceiverActivity : BaseActivity() {

    private val binding by viewBinding(ActivityShareBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val shareReceiverViewModel: ShareReceiverViewModel by viewModels()

        setupViewModels(shareReceiverViewModel)
        intent?.checkForExtraText(shareReceiverViewModel::saveUrl)
    }

    @Suppress("MagicNumber")
    private fun setupViewModels(shareReceiverViewModel: ShareReceiverViewModel) {
        lifecycleScope.launch {
            shareReceiverViewModel.saved.collect { message ->
                binding.imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                toast(message)
                delay(500L)
                finish()
            }
        }
        lifecycleScope.launch {
            shareReceiverViewModel.edit.collect { message ->
                if (message.isNotEmpty()) {
                    binding.imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                    toast(message)
                }
                startActivity(MainActivity.Builder(this@ShareReceiverActivity).build())
                finish()
            }
        }
        lifecycleScope.launch {
            shareReceiverViewModel.failed.collect { error ->
                binding.imageViewFeedback.setImageResource(R.drawable.ic_url_saved_error)

                val loginFailedCodes = listOf(
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    HttpURLConnection.HTTP_INTERNAL_ERROR
                )
                when {
                    error is InvalidUrlException -> {
                        showStyledDialog(
                            dialogStyle = R.style.AppTheme_AlertDialog,
                            dialogBackground = R.drawable.background_contrast_rounded
                        ) {
                            setMessage(R.string.validation_error_invalid_url_rationale)
                            setPositiveButton(R.string.hint_ok) { _, _ -> finish() }
                        }
                    }
                    error.isServerException() -> {
                        showStyledDialog(
                            dialogStyle = R.style.AppTheme_AlertDialog,
                            dialogBackground = R.drawable.background_contrast_rounded
                        ) {
                            setMessage(R.string.server_timeout_error)
                            setPositiveButton(R.string.hint_ok) { _, _ -> finish() }
                        }
                    }
                    error is HttpException && error.code() in loginFailedCodes -> {
                        showStyledDialog(
                            dialogStyle = R.style.AppTheme_AlertDialog,
                            dialogBackground = R.drawable.background_contrast_rounded
                        ) {
                            setMessage(R.string.auth_logged_out_feedback)
                            setPositiveButton(R.string.hint_ok) { _, _ -> finish() }
                        }
                    }
                    else -> sendErrorReport(error) { finish() }
                }
            }
        }
    }

    private fun Intent.checkForExtraText(onExtraTextFound: (String) -> Unit) {
        takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            ?.let(onExtraTextFound)
    }
}
