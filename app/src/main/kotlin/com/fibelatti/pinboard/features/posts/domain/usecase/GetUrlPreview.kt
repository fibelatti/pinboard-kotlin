package com.fibelatti.pinboard.features.posts.domain.usecase

import android.os.Build
import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class GetUrlPreview @Inject constructor(
    private val userRepository: UserRepository,
) : UseCaseWithParams<GetUrlPreview.Params, Result<UrlPreview>> {

    override suspend operator fun invoke(params: Params): Result<UrlPreview> = catching {
        if (userRepository.autoFillDescription || userRepository.followRedirects) {
            loadUrl(params)
        } else {
            createUrlPreview(params)
        }
    }.onFailure(Timber::e)
        .onFailureReturn(createUrlPreview(params))

    private fun createUrlPreview(params: Params): UrlPreview = UrlPreview(
        url = params.url,
        title = params.title.ifNullOrBlank { params.url }.take(AppConfig.PinboardApiMaxLength.TEXT_TYPE.value),
        description = params.highlightedText,
    )

    private suspend fun loadUrl(params: Params): UrlPreview {
        val document: Document = withContext(Dispatchers.IO) {
            Jsoup.connect(params.url)
                .header(
                    /* name = */ "User-Agent",
                    /* value = */ "Pinkt/${BuildConfig.VERSION_NAME} (Android; ${Build.VERSION.SDK_INT})",
                )
                .get()
        }

        val previewUrl: String = if (userRepository.followRedirects) document.location() else params.url

        val previewTitle = (document.getMetaProperty(property = "og:title") ?: document.title())
            .takeIf { userRepository.autoFillDescription }
            .ifNullOrBlank { params.title.ifNullOrBlank { previewUrl } }
        val previewDescription = params.highlightedText
            ?: document.getMetaProperty(property = "og:description").takeIf { userRepository.autoFillDescription }

        return UrlPreview(
            url = previewUrl,
            title = previewTitle.take(AppConfig.PinboardApiMaxLength.TEXT_TYPE.value),
            description = previewDescription,
        )
    }

    private fun Document.getMetaProperty(property: String): String? = select("meta[property=$property]")
        .firstOrNull()
        ?.attr("content")
        ?.ifBlank { null }

    data class Params(
        val url: String,
        val title: String? = null,
        val highlightedText: String? = null,
    )
}
