package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.pinboard.core.di.UrlParser
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class GetUrlPreview @Inject constructor(
    @UrlParser private val okHttpClient: OkHttpClient,
    private val userRepository: UserRepository,
) : UseCaseWithParams<UrlPreview, GetUrlPreview.Params>() {

    override suspend fun run(params: Params): Result<UrlPreview> = catching {
        if (userRepository.autoFillDescription) {
            fetchTitleAndDescription(params)
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

    private suspend fun fetchTitleAndDescription(params: Params): UrlPreview {
        val request = Request.Builder()
            .url(params.url)
            .apply {
                // Prevent async loading of metadata
                if (params.url.contains("twitter.com")) {
                    header(
                        name = "User-Agent",
                        value = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://google.com/bot.html)",
                    )
                }
            }
            .build()

        val (url, document) = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                response.request.url.toString() to Jsoup.parse(requireNotNull(response.body).string())
            }
        }

        val title = document.getMetaProperty(property = "og:title")
            ?: document.title().ifBlank { params.title.ifNullOrBlank { url } }
        val description = params.highlightedText
            ?: document.getMetaProperty(property = "og:description")

        return UrlPreview(url, title, description)
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
