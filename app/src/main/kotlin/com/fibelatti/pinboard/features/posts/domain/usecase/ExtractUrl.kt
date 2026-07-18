package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.ResultUseCaseWithParams
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject

class ExtractUrl @Inject constructor(
    private val userRepository: UserRepository,
) : ResultUseCaseWithParams<String, ExtractUrl.ExtractedUrl> {

    override suspend operator fun invoke(params: String): Result<ExtractedUrl> {
        val schemes = ValidUrlScheme.ALL_SCHEMES.map { "$it://" }
        val firstSchemeIndex = schemes.mapNotNull { scheme -> params.indexOf(scheme).takeIf { it >= 0 } }
            .minOrNull()
            ?: return Result.failure(InvalidUrlException())
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

        return Result.success(
            ExtractedUrl(
                url = cleanUrl,
                highlightedText = highlightedText,
            ),
        )
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
