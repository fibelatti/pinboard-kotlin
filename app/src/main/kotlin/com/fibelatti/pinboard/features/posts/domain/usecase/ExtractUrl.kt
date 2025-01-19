package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.pinboard.features.user.domain.UserRepository
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractUrl @Inject constructor(
    private val userRepository: UserRepository,
) : UseCaseWithParams<String, Result<ExtractUrl.ExtractedUrl>> {

    override suspend operator fun invoke(params: String): Result<ExtractedUrl> {
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

        val parameters = buildList {
            if (userRepository.removeUtmParameters) {
                add("utm")
            }
            addAll(userRepository.removedUrlParameters)
        }
        val cleanUrl = if (parameters.isNotEmpty()) {
            removeQueryParameters(url = sourceUrl, parameters = parameters)
        } else {
            sourceUrl
        }

        return try {
            Success(
                ExtractedUrl(
                    url = withContext(Dispatchers.IO) { URLDecoder.decode(cleanUrl, "UTF-8") },
                    highlightedText = highlightedText,
                ),
            )
        } catch (_: UnsupportedEncodingException) {
            Failure(InvalidUrlException())
        }
    }

    private fun removeQueryParameters(url: String, parameters: List<String>): String {
        val cleanUrl: String = parameters.fold(url) { currentUrl, parameter ->
            currentUrl.replace(regex = Regex(pattern = "&?$parameter[^&]*"), replacement = "")
        }

        return cleanUrl.replace(oldValue = "?&", newValue = "?").removeSuffix("?")
    }

    data class ExtractedUrl(
        val url: String,
        val highlightedText: String? = null,
    )
}
