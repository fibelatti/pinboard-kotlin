package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.ResultUseCaseWithParams
import com.fibelatti.core.functional.coRunCatching
import com.fibelatti.core.platform.UserAgentProvider
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

class GetUrlPreview @Inject constructor(
    @RestApi(RestApiProvider.WEBSITE) private val httpClient: HttpClient,
    private val userRepository: UserRepository,
    private val userAgentProvider: UserAgentProvider,
) : ResultUseCaseWithParams<GetUrlPreview.Params, UrlPreview> {

    override suspend operator fun invoke(params: Params): Result<UrlPreview> = coRunCatching {
        if (userRepository.autoFillDescription || userRepository.followRedirects) {
            loadUrl(params)
        } else {
            createUrlPreview(params)
        }
    }
        .onFailure(Timber::e)
        .recover { createUrlPreview(params) }

    private fun createUrlPreview(params: Params): UrlPreview = UrlPreview(
        url = params.url,
        title = params.title.ifNullOrBlank { params.url }.take(AppConfig.PinboardApiMaxLength.TEXT_TYPE.value),
        description = params.highlightedText,
    )

    private suspend fun loadUrl(params: Params): UrlPreview {
        val response: HttpResponse = httpClient.get(urlString = params.url) {
            header(HttpHeaders.UserAgent, userAgentProvider.userAgent)
        }

        if (!response.status.isSuccess()) {
            throw UrlPreviewFailedException(url = params.url, status = response.status.value)
        }

        if ((response.contentLength() ?: 0L) > MAX_PAGE_BYTES) {
            throw UrlPreviewFailedException(url = params.url, status = null)
        }

        val pageBytes: ByteArray = response.bodyAsBytes()

        if (pageBytes.size > MAX_PAGE_BYTES) {
            throw UrlPreviewFailedException(url = params.url, status = null)
        }

        // The URL the response actually came from, which is where redirects landed.
        val call: HttpClientCall = response.call
        val resolvedUrl: String = call.request.url.toString()

        val document: Document = withContext(Dispatchers.IO) {
            // Parsed from bytes rather than a String so jsoup can sniff the encoding from the BOM
            // and the <meta charset> tag. Most pages omit the charset from the Content-Type header,
            // and Ktor would then assume UTF-8 and mangle every non-UTF-8 site.
            Jsoup.parse(/* in = */ pageBytes.inputStream(), /* charsetName = */ null, /* baseUri = */ resolvedUrl)
        }

        val previewUrl: String = if (userRepository.followRedirects) resolvedUrl else params.url

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

    private companion object {

        /**
         * Matches the default jsoup applied when it was fetching the page itself. A preview only needs the document
         * head, so there is no reason to buffer a whole document past this size.
         */
        const val MAX_PAGE_BYTES: Int = 2 * 1024 * 1024
    }
}

class UrlPreviewFailedException(url: String, status: Int?) : Exception(
    "Failed to load a preview for $url" + status?.let { " (HTTP $it)" }.orEmpty(),
)
