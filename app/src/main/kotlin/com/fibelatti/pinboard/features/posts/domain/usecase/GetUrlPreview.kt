package com.fibelatti.pinboard.features.posts.domain.usecase

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
) : UseCaseWithParams<UrlPreview, String>() {

    override suspend fun run(params: String): Result<UrlPreview> = catching {
        val request = Request.Builder()
            .url(params)
            .apply {
                // Prevent async loading of metadata
                if (params.contains("twitter.com")) {
                    header("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://google.com/bot.html)")
                }
            }
            .build()

        val (url, document) = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                response.request.url.toString() to Jsoup.parse(requireNotNull(response.body).string())
            }
        }

        val title = document.getMetaProperty("og:title") ?: document.title().ifBlank { url }
        val description = document.getMetaProperty("og:description").takeIf { userRepository.autoFillDescription }

        UrlPreview(url, title, description)
    }.onFailureReturn(UrlPreview(url = params, title = params))

    private fun Document.getMetaProperty(
        property: String,
    ): String? = select("meta[property=$property]").firstOrNull()?.attr("content")?.ifBlank { null }
}
