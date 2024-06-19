package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

@Factory
class ExtractUrl : UseCaseWithParams<ExtractUrl.ExtractedUrl, String>() {

    override suspend fun run(params: String): Result<ExtractedUrl> {
        val schemes = ValidUrlScheme.ALL_SCHEMES.map { "$it://" }
        val firstSchemeIndex = schemes.mapNotNull { scheme -> params.indexOf(scheme).takeIf { it >= 0 } }
            .minOrNull()
            ?: return Failure(InvalidUrlException())
        val sourceUrl = params.substring(startIndex = firstSchemeIndex)
            .substringBefore(delimiter = "#:~:text=")
        val highlightedText = params.substring(startIndex = 0, endIndex = firstSchemeIndex)
            .trim()
            .takeIf { it.startsWith("\"") && it.endsWith("\"") }
            ?.let { it.substring(startIndex = 1, endIndex = it.length - 1) }

        return try {
            Success(
                ExtractedUrl(
                    url = withContext(Dispatchers.IO) { URLDecoder.decode(sourceUrl, "UTF-8") },
                    highlightedText = highlightedText,
                ),
            )
        } catch (ignored: UnsupportedEncodingException) {
            Failure(InvalidUrlException())
        }
    }

    data class ExtractedUrl(
        val url: String,
        val highlightedText: String? = null,
    )
}
