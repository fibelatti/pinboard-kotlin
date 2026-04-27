package com.fibelatti.pinboard.features.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.main.MainComposeActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import timber.log.Timber

class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resourceProvider: ResourceProvider,
) {

    private val notificationManager: NotificationManager? = context.getSystemService<NotificationManager>()

    init {
        if (notificationManager == null) {
            Timber.w("Platform NotificationManager is null, method will not work as expected!")
        }
    }

    fun createNotificationChannels() {
        if (notificationManager == null) return

        val notificationChannel: NotificationChannel = NotificationChannel(
            SHARE_RECEIVER_NOTIFICATION_CHANNEL_ID,
            resourceProvider.getString(R.string.share_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = resourceProvider.getString(R.string.share_notification_channel_description)
        }

        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun createShareReceiverNotification(
        notificationId: Int,
        title: String,
        text: String,
        postId: String? = null,
    ): Notification {
        val notificationBuilder = NotificationCompat.Builder(context, SHARE_RECEIVER_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_pin)

        if (postId == null) {
            return notificationBuilder.build()
        }

        val viewBookmarkIntent: Intent = MainComposeActivity.Builder(context)
            .notificationExtras(notificationId = notificationId, postId = postId, openEditor = false)
            .build()
        val viewBookmarkPendingIntent: PendingIntent = PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ notificationId + 1,
            /* intent = */ viewBookmarkIntent,
            /* flags = */ PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val editBookmarkIntent: Intent = MainComposeActivity.Builder(context)
            .notificationExtras(notificationId = notificationId, postId = postId, openEditor = true)
            .build()
        val editBookmarkPendingIntent: PendingIntent = PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ notificationId + 2,
            /* intent = */ editBookmarkIntent,
            /* flags = */ PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val editBookmarkAction = NotificationCompat.Action.Builder(
            null,
            context.getString(R.string.quick_actions_edit),
            editBookmarkPendingIntent,
        ).build()

        notificationBuilder
            .setContentIntent(viewBookmarkPendingIntent)
            .addAction(editBookmarkAction)

        return notificationBuilder.build()
    }

    fun sendNotification(notificationId: Int, notification: Notification) {
        if (context.isNotificationPermissionGranted()) {
            notificationManager?.notify(notificationId, notification)
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager?.cancel(notificationId)
    }

    private companion object {

        private const val SHARE_RECEIVER_NOTIFICATION_CHANNEL_ID = "share_receiver_feedback_channel"
    }
}

fun Context.isNotificationPermissionGranted(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
