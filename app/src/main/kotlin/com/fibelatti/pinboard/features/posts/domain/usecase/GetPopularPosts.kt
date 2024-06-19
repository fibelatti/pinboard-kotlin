package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.koin.core.annotation.Factory
import org.koin.core.annotation.ScopeId
import java.util.UUID
import javax.inject.Inject

@Factory
class GetPopularPosts @Inject constructor(
    @ScopeId(name = "base") @RestApi(RestApiProvider.BASE) private val httpClient: HttpClient,
) : UseCase<List<Post>>() {

    override suspend fun run(): Result<List<Post>> = catching {
        val document = withContext(Dispatchers.IO) {
            Jsoup.parse(httpClient.get(urlString = "https://pinboard.in/popular/").bodyAsText())
        }

        document.select(".bookmark").mapNotNull { element ->
            val (url, title) = element.select(".bookmark_title").firstOrNull()
                ?.let { it.attr("href") to it.text() }
                ?: return@mapNotNull null
            val tags = element.select(".tag").map { Tag(it.text()) }.ifEmpty { null }

            Post(
                url = url,
                title = title,
                description = "",
                id = UUID.randomUUID().toString(),
                private = false,
                readLater = false,
                tags = tags,
            )
        }
    }
}
