package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.pinboard.core.network.InvalidRequestException
import javax.inject.Inject

class ValidateUrl @Inject constructor() : UseCaseWithParams<String, String>() {

    override suspend fun run(params: String): Result<String> {
        return if (params.substringBefore("://", "") !in UrlValidSchemes.allSchemes()) {
            Failure(InvalidRequestException())
        } else {
            Success(params)
        }
    }
}

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
