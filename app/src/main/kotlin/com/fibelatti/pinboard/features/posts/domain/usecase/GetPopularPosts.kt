package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.core.di.UrlParser
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.UUID
import javax.inject.Inject

class GetPopularPosts @Inject constructor(
    @UrlParser private val okHttpClient: OkHttpClient,
) : UseCase<List<Post>>() {

    override suspend fun run(): Result<List<Post>> = catching {
        val request = Request.Builder().url("https://pinboard.in/popular/").build()

        val document = withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                Jsoup.parse(requireNotNull(response.body).string())
            }
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
