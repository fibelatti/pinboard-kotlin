package com.fibelatti.pinboard.features.share

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.functional.isSuccess
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.functional.throwOnFailure
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.main.MainComposeActivity
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.GetUrlPreview
import com.fibelatti.pinboard.features.posts.domain.usecase.UrlPreview
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.random.Random

@HiltWorker
class ShareReceiverWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val extractUrl: ExtractUrl,
    private val getUrlPreview: GetUrlPreview,
    private val addPost: AddPost,
    private val postsRepository: PostsRepository,
    private val userRepository: UserRepository,
) : CoroutineWorker(context, workerParams) {

    private val notificationManager: NotificationManager? = applicationContext.getSystemService<NotificationManager>()
    private val notificationId: Int = Random.nextInt()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationId,
            createNotification(
                title = applicationContext.getString(R.string.share_notification_title_foreground),
                text = inputData.getString(BUNDLE_KEY_URL).orEmpty(),
            ),
        )
    }

    override suspend fun doWork(): Result {
        val url = inputData.getString(BUNDLE_KEY_URL) ?: return Result.failure()
        val title = inputData.getString(BUNDLE_KEY_TITLE).orEmpty()

        val result = extractUrl(inputUrl = url)
            .mapCatching { (extractedUrl: String, highlightedText: String?) ->
                val getPreviewParams = GetUrlPreview.Params(
                    url = extractedUrl,
                    title = title,
                    highlightedText = highlightedText,
                )

                val urlPreview: UrlPreview = getUrlPreview(params = getPreviewParams)
                    .getOrThrow()

                val existingPost: Post? = postsRepository.getPost(id = "", url = urlPreview.url)
                    .getOrNull()

                if (existingPost != null) {
                    sendNotification(
                        notification = createNotification(
                            title = applicationContext.getString(R.string.share_notification_title_existing),
                            text = existingPost.title,
                            postId = existingPost.id,
                        ),
                    )

                    return@mapCatching
                }

                val newPost = Post(
                    url = urlPreview.url,
                    title = title,
                    description = urlPreview.description
                        ?.let { if (userRepository.useBlockquote) "<blockquote>$it</blockquote>" else it }
                        .orEmpty(),
                    private = userRepository.defaultPrivate,
                    readLater = userRepository.defaultReadLater,
                    tags = userRepository.defaultTags,
                )

                addPost(params = newPost)
                    .onSuccess { post ->
                        sendNotification(
                            notification = createNotification(
                                title = applicationContext.getString(R.string.share_notification_title_success),
                                text = post.title,
                                postId = post.id,
                            ),
                        )
                    }
                    .throwOnFailure()
            }.onFailure {
                sendNotification(
                    notification = createNotification(
                        title = applicationContext.getString(R.string.share_notification_title_failure),
                        text = url,
                    ),
                )
            }

        return if (result.isSuccess) Result.success() else Result.failure()
    }

    private fun createNotification(
        title: String,
        text: String,
        postId: String? = null,
    ): Notification {
        val notificationBuilder = Notification.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_pin)

        if (postId != null) {
            val viewBookmarkIntent: Intent = MainComposeActivity.Builder(applicationContext)
                .notificationExtras(postId = postId, notificationId = notificationId)
                .build()
            val viewBookmarkPendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */ applicationContext,
                /* requestCode = */ notificationId,
                /* intent = */ viewBookmarkIntent,
                /* flags = */ PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            val editBookmarkIntent: Intent = MainComposeActivity.Builder(applicationContext)
                .notificationExtras(postId = postId, notificationId = notificationId)
                .build()
            val editBookmarkPendingIntent: PendingIntent = PendingIntent.getActivity(
                /* context = */ applicationContext,
                /* requestCode = */ notificationId,
                /* intent = */ editBookmarkIntent,
                /* flags = */ PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val editBookmarkAction = Notification.Action.Builder(
                null,
                applicationContext.getString(R.string.quick_actions_edit),
                editBookmarkPendingIntent,
            ).build()

            notificationBuilder
                .setContentIntent(viewBookmarkPendingIntent)
                .addAction(editBookmarkAction)
        }

        return notificationBuilder.build()
    }

    private fun sendNotification(notification: Notification) {
        val result = applicationContext.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
        if (result == PackageManager.PERMISSION_GRANTED) {
            notificationManager?.notify(notificationId, notification)
        }
    }

    companion object {

        private const val BUNDLE_KEY_URL = "BK_URL"
        private const val BUNDLE_KEY_TITLE = "BK_TITLE"

        const val NOTIFICATION_CHANNEL_ID = "share_receiver_feedback_channel"

        fun workRequest(
            url: String,
            title: String?,
        ): WorkRequest {
            return OneTimeWorkRequestBuilder<ShareReceiverWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(BUNDLE_KEY_URL to url, BUNDLE_KEY_TITLE to title))
                .build()
        }
    }
}
