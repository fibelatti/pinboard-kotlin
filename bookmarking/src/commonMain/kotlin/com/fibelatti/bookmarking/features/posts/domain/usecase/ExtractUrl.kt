package com.fibelatti.bookmarking.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.koin.core.annotation.Factory

@Factory
public class ExtractUrl : UseCaseWithParams<ExtractUrl.ExtractedUrl, String>() {

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
                    url = withContext(Dispatchers.IO) { UrlEncoderUtil.decode(sourceUrl) },
                    highlightedText = highlightedText,
                ),
            )
        } catch (ignored: Exception) {
            Failure(InvalidUrlException())
        }
    }

    public data class ExtractedUrl(
        val url: String,
        val highlightedText: String? = null,
    )
}
