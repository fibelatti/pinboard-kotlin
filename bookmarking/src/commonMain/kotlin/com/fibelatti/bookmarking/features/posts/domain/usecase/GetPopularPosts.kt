package com.fibelatti.bookmarking.features.posts.domain.usecase

import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.core.functional.catching
import com.fibelatti.core.randomUUID
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
public class GetPopularPosts(
    @Named("base") private val httpClient: HttpClient,
) : UseCase<List<Post>>() {

    override suspend fun run(): Result<List<Post>> = catching {
        val document = withContext(Dispatchers.IO) {
            Ksoup.parse(httpClient.get(urlString = "https://pinboard.in/popular/").bodyAsText())
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
                id = randomUUID(),
                private = false,
                readLater = false,
                tags = tags,
            )
        }
    }
}
