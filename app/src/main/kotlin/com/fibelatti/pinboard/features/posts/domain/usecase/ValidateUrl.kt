package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import javax.inject.Inject

class ValidateUrl @Inject constructor() : UseCaseWithParams<String, String>() {

    private val urlRegex = """[-a-zA-Z0-9@:%._+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_+.~#?&/=]*)""".toRegex()

    override suspend fun run(params: String): Result<String> =
        if (validate(params)) Success(params) else Failure(InvalidUrlException())

    private fun validate(url: String): Boolean =
        url.substringBefore("://", "") in UrlValidSchemes.allSchemes() &&
            url.substringAfter("://", "").matches(urlRegex)
}

class InvalidUrlException : Throwable()

enum class UrlValidSchemes(val scheme: String) {
    HTTP("http"),
    HTTPS("https"),
    JAVASCRIPT("javascript"),
    MAILTO("mailto"),
    FTP("ftp"),
    FILE("file");

    companion object {
        @JvmStatic
        fun allSchemes() = listOf(
            HTTP.scheme,
            HTTPS.scheme,
            JAVASCRIPT.scheme,
            MAILTO.scheme,
            FTP.scheme,
            FILE.scheme
        )
    }
}
