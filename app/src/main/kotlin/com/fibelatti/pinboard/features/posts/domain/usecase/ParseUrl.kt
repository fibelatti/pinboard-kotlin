package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class ParseUrl @Inject constructor() : UseCaseWithParams<RichUrl, String>() {

    override suspend fun run(params: String): Result<RichUrl> = withContext(Dispatchers.IO) {
        Success(
            catching {
                val document = Jsoup.connect(params).get()

                val url = document.location()
                val title = document.title()
                val imageUrl: String? = document.getElementsByTag("meta")
                    .find { it.attr("property") == "og:image" }
                    ?.attr("content")

                RichUrl(url, title, imageUrl)
            }.getOrDefault(RichUrl(url = params, title = params))
        )
    }
}
