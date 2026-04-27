package com.fibelatti.pinboard.features.share

import android.content.Context
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
import com.fibelatti.pinboard.features.notifications.AppNotificationManager
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.GetUrlPreview
import com.fibelatti.pinboard.features.posts.domain.usecase.UrlPreview
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ShareReceiverWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val extractUrl: ExtractUrl,
    private val getUrlPreview: GetUrlPreview,
    private val addPost: AddPost,
    private val postsRepository: PostsRepository,
    private val userRepository: UserRepository,
    private val appNotificationManager: AppNotificationManager,
) : CoroutineWorker(context, workerParams) {

    private val notificationId: Int = id.hashCode()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationId,
            appNotificationManager.createShareReceiverNotification(
                notificationId = notificationId,
                title = applicationContext.getString(R.string.share_notification_title_foreground),
                text = inputData.getString(BUNDLE_KEY_URL).orEmpty(),
            ),
        )
    }

    override suspend fun doWork(): Result {
        val url: String = inputData.getString(BUNDLE_KEY_URL) ?: return Result.failure()
        val title: String = inputData.getString(BUNDLE_KEY_TITLE).orEmpty()

        val result = processBookmark(url = url, title = title)

        return if (result.isSuccess) Result.success() else Result.failure()
    }

    private suspend fun processBookmark(
        url: String,
        title: String,
    ): com.fibelatti.core.functional.Result<Unit> {
        appNotificationManager.sendNotification(
            notificationId = notificationId,
            notification = appNotificationManager.createShareReceiverNotification(
                notificationId = notificationId,
                title = applicationContext.getString(R.string.share_notification_title_foreground),
                text = inputData.getString(BUNDLE_KEY_URL).orEmpty(),
            ),
        )

        return extractUrl(inputUrl = url)
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
                    appNotificationManager.sendNotification(
                        notificationId = notificationId,
                        notification = appNotificationManager.createShareReceiverNotification(
                            notificationId = notificationId,
                            title = applicationContext.getString(
                                R.string.share_notification_title_existing,
                                existingPost.displayDateAdded,
                            ),
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
                        appNotificationManager.sendNotification(
                            notificationId = notificationId,
                            notification = appNotificationManager.createShareReceiverNotification(
                                notificationId = notificationId,
                                title = applicationContext.getString(R.string.share_notification_title_success),
                                text = post.title,
                                postId = post.id,
                            ),
                        )
                    }
                    .throwOnFailure()
            }.onFailure {
                appNotificationManager.sendNotification(
                    notificationId = notificationId,
                    notification = appNotificationManager.createShareReceiverNotification(
                        notificationId = notificationId,
                        title = applicationContext.getString(R.string.share_notification_title_failure),
                        text = url,
                    ),
                )
            }
    }

    companion object {

        private const val BUNDLE_KEY_URL = "BK_URL"
        private const val BUNDLE_KEY_TITLE = "BK_TITLE"

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
