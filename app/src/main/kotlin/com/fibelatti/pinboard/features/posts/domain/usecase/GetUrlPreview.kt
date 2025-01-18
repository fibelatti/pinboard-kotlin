package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.userAgent
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GetUrlPreview @Inject constructor(
    @RestApi(RestApiProvider.BASE) private val httpClient: HttpClient,
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
        title = params.title.ifNullOrBlank { params.url }.take(AppConfig.PinboardApiMaxLength.TEXT_TYPE.value),
        description = params.highlightedText,
    )

    private suspend fun loadUrl(params: Params): UrlPreview {
        val response = withContext(Dispatchers.IO) {
            httpClient.get(params.url) {
                if (params.url.run { contains("twitter.com") || contains("x.com") }) {
                    userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://google.com/bot.html)")
                }
            }
        }

        val previewUrl: String = if (userRepository.followRedirects) response.request.url.toString() else params.url
        val document: Document = Jsoup.parse(response.bodyAsText())

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
