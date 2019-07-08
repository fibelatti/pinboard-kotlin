package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test

internal class ValidUrlSchemeTest {

    @Test
    fun `ValidUrlScheme values should be correct`() {
        ValidUrlScheme.HTTP.scheme shouldBe "http"
        ValidUrlScheme.HTTPS.scheme shouldBe "https"
        ValidUrlScheme.JAVASCRIPT.scheme shouldBe "javascript"
        ValidUrlScheme.MAILTO.scheme shouldBe "mailto"
        ValidUrlScheme.FTP.scheme shouldBe "ftp"
        ValidUrlScheme.FILE.scheme shouldBe "file"
    }

    @Test
    fun `allSchemes should include all schemes`() {
        ValidUrlScheme.allSchemes() shouldBe listOf(
            ValidUrlScheme.HTTP.scheme,
            ValidUrlScheme.HTTPS.scheme,
            ValidUrlScheme.JAVASCRIPT.scheme,
            ValidUrlScheme.MAILTO.scheme,
            ValidUrlScheme.FTP.scheme,
            ValidUrlScheme.FILE.scheme
        )
    }
}
