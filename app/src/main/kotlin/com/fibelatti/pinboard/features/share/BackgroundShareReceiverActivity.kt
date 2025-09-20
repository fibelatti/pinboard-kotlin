package com.fibelatti.pinboard.features.share

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.work.WorkManager
import com.fibelatti.pinboard.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackgroundShareReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentReader = ShareCompat.IntentReader(this)
        val url = intentReader.text.toString().ifEmpty {
            Toast.makeText(
                this,
                R.string.share_notification_unprocessable_url_feedback,
                Toast.LENGTH_SHORT,
            ).show()
            finish()
            return
        }

        // Check for multiple services and fallback to default share UX

        val result = applicationContext.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
        if (result != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                R.string.share_notification_missing_permission_feedback,
                Toast.LENGTH_LONG,
            ).show()
        }

        val workRequest = ShareReceiverWorker.workRequest(
            url = url,
            title = intentReader.subject,
        )

        WorkManager.getInstance(this).enqueue(workRequest)

        finish()
    }
}
