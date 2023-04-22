package com.fibelatti.pinboard.features.posts.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ValidUrlSchemeTest {

    @Test
    fun `ValidUrlScheme values should be correct`() {
        assertThat(ValidUrlScheme.HTTP.scheme).isEqualTo("http")
        assertThat(ValidUrlScheme.HTTPS.scheme).isEqualTo("https")
        assertThat(ValidUrlScheme.JAVASCRIPT.scheme).isEqualTo("javascript")
        assertThat(ValidUrlScheme.MAILTO.scheme).isEqualTo("mailto")
        assertThat(ValidUrlScheme.FTP.scheme).isEqualTo("ftp")
        assertThat(ValidUrlScheme.FILE.scheme).isEqualTo("file")
    }

    @Test
    fun `allSchemes should include all schemes`() {
        assertThat(ValidUrlScheme.ALL_SCHEMES).isEqualTo(
            listOf(
                ValidUrlScheme.HTTP.scheme,
                ValidUrlScheme.HTTPS.scheme,
                ValidUrlScheme.JAVASCRIPT.scheme,
                ValidUrlScheme.MAILTO.scheme,
                ValidUrlScheme.FTP.scheme,
                ValidUrlScheme.FILE.scheme,
            ),
        )
    }
}
