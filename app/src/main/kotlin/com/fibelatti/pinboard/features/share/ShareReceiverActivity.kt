package com.fibelatti.pinboard.features.share

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.showStyledDialog
import com.fibelatti.core.extension.toast
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.features.MainActivity
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.net.HttpURLConnection

class ShareReceiverActivity : BaseActivity(R.layout.activity_share) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val shareReceiverViewModel by viewModel { viewModelProvider.shareReceiverViewModel() }

        setupViewModels(shareReceiverViewModel)
        intent?.checkForExtraText(shareReceiverViewModel::saveUrl)
    }

    private fun setupViewModels(shareReceiverViewModel: ShareReceiverViewModel) {
        with(shareReceiverViewModel) {
            observeEvent(saved) { message ->
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                toast(message)
                finish()
            }
            observeEvent(edit) { message ->
                if (message.isNotEmpty()) {
                    imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                    toast(message)
                }
                startActivity(MainActivity.Builder(this@ShareReceiverActivity).build())
                finish()
            }
            observeEvent(failed) { error ->
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved_error)

                val loginFailedCodes = listOf(
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    HttpURLConnection.HTTP_INTERNAL_ERROR
                )
                when {
                    error is TimeoutCancellationException -> {
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
