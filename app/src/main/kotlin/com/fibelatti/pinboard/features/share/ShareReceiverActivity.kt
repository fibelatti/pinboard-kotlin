package com.fibelatti.pinboard.features.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.databinding.ActivityShareBinding
import com.fibelatti.pinboard.features.MainActivity
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
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
        shareReceiverViewModel.saved
            .onEach { message ->
                binding.imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                delay(500L)
                finish()
            }
            .launchInAndFlowWith(this)
        shareReceiverViewModel.edit
            .onEach { message ->
                if (message.isNotEmpty()) {
                    binding.imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
                startActivity(MainActivity.Builder(this).build())
                finish()
            }
            .launchInAndFlowWith(this)
        shareReceiverViewModel.failed
            .onEach { error ->
                binding.imageViewFeedback.setImageResource(R.drawable.ic_url_saved_error)

                val loginFailedCodes = listOf(
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                )
                val errorMessage = when {
                    error is InvalidUrlException -> R.string.validation_error_invalid_url_rationale
                    error.isServerException() -> R.string.server_timeout_error
                    error is HttpException && error.code() in loginFailedCodes -> R.string.auth_logged_out_feedback
                    else -> {
                        sendErrorReport(error) { finish() }
                        return@onEach
                    }
                }

                MaterialAlertDialogBuilder(this)
                    .apply {
                        setMessage(errorMessage)
                        setPositiveButton(R.string.hint_ok) { _, _ -> finish() }
                    }
                    .show()
            }
            .launchInAndFlowWith(this)
    }

    private fun Intent.checkForExtraText(onExtraTextFound: (String) -> Unit) {
        takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            ?.let(onExtraTextFound)
    }
}
