package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import javax.inject.Inject

class ParseUrl @Inject constructor(
    private val userRepository: UserRepository
) : UseCaseWithParams<RichUrl, String>() {

    override suspend fun run(params: String): Result<RichUrl> {
        return catching {
            val document = withContext(Dispatchers.IO) { Jsoup.connect(params).get() }
            val url = document.location()
            val title = document.title()
            val description: String? = document.getMetaProperty("og:description")
                .takeIf { userRepository.getAutoFillDescription() }
            val imageUrl: String? = document.getMetaProperty("og:image")

            RichUrl(url, title, description, imageUrl)
        }.onFailureReturn(RichUrl(url = params, title = params))
    }

    private fun Document.getMetaProperty(property: String): String? =
        getElementsByTag("meta")
            .find { it.attr("property") == property }
            ?.attr("content")
}
