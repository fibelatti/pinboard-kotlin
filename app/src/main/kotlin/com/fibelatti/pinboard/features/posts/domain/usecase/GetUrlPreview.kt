package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.pinboard.core.di.UrlParser
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GetUrlPreview @Inject constructor(
    @UrlParser private val okHttpClient: OkHttpClient,
    private val userRepository: UserRepository,
) : UseCaseWithParams<GetUrlPreview.Params, Result<UrlPreview>> {

    override suspend operator fun invoke(params: Params): Result<UrlPreview> = catching {
        if (userRepository.autoFillDescription || userRepository.followRedirects) {
            loadUrl(params)
        } else {
            createUrlPreview(params)
        }
    }.onFailureReturn(
        createUrlPreview(params),
    )

    private fun createUrlPreview(params: Params): UrlPreview = UrlPreview(
        url = params.url,
        title = params.title.ifNullOrBlank { params.url },
        description = params.highlightedText,
    )

    private suspend fun loadUrl(params: Params): UrlPreview {
        val request = Request.Builder()
            .url(params.url)
            .apply {
                // Prevent async loading of metadata
                if (params.url.run { contains("twitter.com") || contains("x.com") }) {
                    header(
                        name = "User-Agent",
                        value = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://google.com/bot.html)",
                    )
                }
            }
            .build()

        val previewUrl: String
        val document: Document

        withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                previewUrl = if (userRepository.followRedirects) response.request.url.toString() else params.url
                document = Jsoup.parse(requireNotNull(response.body).string())
            }
        }

        val previewTitle = (document.getMetaProperty(property = "og:title") ?: document.title())
            .takeIf { userRepository.autoFillDescription }
            .ifNullOrBlank { params.title.ifNullOrBlank { previewUrl } }
        val previewDescription = params.highlightedText
            ?: document.getMetaProperty(property = "og:description").takeIf { userRepository.autoFillDescription }

        return UrlPreview(
            url = previewUrl,
            title = previewTitle,
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
