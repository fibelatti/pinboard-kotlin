package com.fibelatti.bookmarking.features.posts.domain.usecase

internal enum class ValidUrlScheme(val scheme: String) {
    HTTP("http"),
    HTTPS("https"),
    JAVASCRIPT("javascript"),
    MAILTO("mailto"),
    FTP("ftp"),
    FILE("file"),
    ;

    companion object {

        @JvmStatic
        val ALL_SCHEMES = listOf(
            HTTP.scheme,
            HTTPS.scheme,
            JAVASCRIPT.scheme,
            MAILTO.scheme,
            FTP.scheme,
            FILE.scheme,
        )
    }
}

public class InvalidUrlException : Throwable()
