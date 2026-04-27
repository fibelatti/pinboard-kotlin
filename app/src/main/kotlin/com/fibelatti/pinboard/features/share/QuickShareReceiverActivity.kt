package com.fibelatti.pinboard.features.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.notifications.isNotificationPermissionGranted
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import timber.log.Timber

@AndroidEntryPoint
class QuickShareReceiverActivity : AppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentReader = ShareCompat.IntentReader(this)
        processRequest(url = intentReader.text.toString(), title = intentReader.subject)
        finish()
    }

    private fun processRequest(url: String, title: String?) {
        when {
            url.isBlank() -> {
                Toast.makeText(
                    /* context = */ this,
                    /* resId = */ R.string.share_notification_unprocessable_url_feedback,
                    /* duration = */ Toast.LENGTH_SHORT,
                ).show()
            }

            isBackgroundEligible() -> {
                if (!isNotificationPermissionGranted()) {
                    showMissingPermissionWarning()
                }

                enqueueWork(url = url, title = title)
            }

            else -> {
                fallbackToQuickShareReceiver()
            }
        }
    }

    private fun isBackgroundEligible(): Boolean {
        return userRepository.useBackgroundShareReceiver &&
            userRepository.userCredentials.value.getConnectedServices().size == 1
    }

    private fun showMissingPermissionWarning() {
        Toast.makeText(
            /* context = */ this,
            /* resId = */ R.string.share_notification_missing_permission_feedback,
            /* duration = */ Toast.LENGTH_LONG,
        ).show()
    }

    private fun enqueueWork(url: String, title: String?) {
        Timber.i("Enqueueing work (url=$url,title=$title)")

        val workRequest: WorkRequest = ShareReceiverWorker.workRequest(url = url, title = title)
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    private fun fallbackToQuickShareReceiver() {
        Timber.i("Not eligible to process in the background, using fallback.")

        val intent: Intent = ShareReceiverActivity.quickShareIntent(context = this, intent = intent)
        startActivity(intent)
    }
}
