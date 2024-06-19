package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.userAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.koin.core.annotation.Factory
import org.koin.core.annotation.ScopeId
import javax.inject.Inject

@Factory
class GetUrlPreview @Inject constructor(
    @ScopeId(name = "base") @RestApi(RestApiProvider.BASE) private val httpClient: HttpClient,
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
        val (url, document) = withContext(Dispatchers.IO) {
            val response = httpClient.get(params.url) {
                if (params.url.run { contains("twitter.com") || contains("x.com") }) {
                    userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://google.com/bot.html)")
                }
            }

            response.request.url.toString() to Jsoup.parse(response.bodyAsText())
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
