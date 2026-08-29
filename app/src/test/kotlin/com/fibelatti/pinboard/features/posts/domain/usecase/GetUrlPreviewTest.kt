package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.pinboard.core.network.UserAgentProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GetUrlPreviewTest {

    private lateinit var server: MockWebServer

    private val fakeUserAgent = "Pinkt/1.0 (Android; 36)"

    private val mockUserRepository: UserRepository = mockk {
        every { autoFillDescription } returns true
        every { followRedirects } returns true
    }

    private val getUrlPreview = GetUrlPreview(
        userRepository = mockUserRepository,
        userAgentProvider = object : UserAgentProvider {
            override val userAgent: String = fakeUserAgent
        },
    )

    @BeforeEach
    fun setup() {
        server = MockWebServer().apply { start() }
    }

    @AfterEach
    fun tearDown() {
        server.close()
    }

    @Test
    fun `WHEN the page is loaded THEN the request is sent with the app user agent`() = runTest {
        server.enqueue(
            MockResponse(code = 200)
                .newBuilder()
                .addHeader("Content-Type", "text/html")
                .body("<html><head><title>Page title</title></head><body></body></html>")
                .build(),
        )

        val result = getUrlPreview(GetUrlPreview.Params(url = server.url("/post").toString()))

        assertThat(result.getOrThrow().title).isEqualTo("Page title")
        assertThat(server.takeRequest().headers["User-Agent"]).isEqualTo(fakeUserAgent)
    }
}
