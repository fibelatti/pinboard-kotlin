package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.platform.UserAgentProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
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

    /**
     * Configured like the client the app injects: redirects are followed by Ktor, not by the engine.
     */
    private val httpClient = HttpClient(OkHttp) {
        install(HttpRedirect) {
            allowHttpsDowngrade = true
        }
        engine {
            config {
                followRedirects(false)
                followSslRedirects(false)
            }
        }
    }

    private val getUrlPreview = GetUrlPreview(
        httpClient = httpClient,
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
        server.enqueue(htmlResponse(html(title = "Page title")))

        val result = getUrlPreview(GetUrlPreview.Params(url = server.url("/post").toString()))

        assertThat(result.getOrThrow().title).isEqualTo("Page title")
        assertThat(server.takeRequest().headers["User-Agent"]).isEqualTo(fakeUserAgent)
    }

    @Test
    fun `WHEN the preview metadata is present THEN it takes precedence over the document`() = runTest {
        server.enqueue(
            htmlResponse(
                """
                <html><head>
                    <title>Document title</title>
                    <meta property="og:title" content="Metadata title">
                    <meta property="og:description" content="Metadata description">
                </head><body></body></html>
                """.trimIndent(),
            ),
        )

        val preview = getUrlPreview(GetUrlPreview.Params(url = server.url("/post").toString())).getOrThrow()

        assertThat(preview.title).isEqualTo("Metadata title")
        assertThat(preview.description).isEqualTo("Metadata description")
    }

    @Test
    fun `WHEN redirects are followed THEN the preview points at the resolved url`() = runTest {
        server.enqueue(redirectResponse(location = "/final"))
        server.enqueue(htmlResponse(html(title = "Page title")))

        val preview = getUrlPreview(GetUrlPreview.Params(url = server.url("/start").toString())).getOrThrow()

        assertThat(preview.url).isEqualTo(server.url("/final").toString())
        assertThat(preview.title).isEqualTo("Page title")
    }

    @Test
    fun `WHEN redirects are not followed THEN the preview points at the requested url`() = runTest {
        every { mockUserRepository.followRedirects } returns false

        server.enqueue(redirectResponse(location = "/final"))
        server.enqueue(htmlResponse(html(title = "Page title")))

        val requestedUrl = server.url("/start").toString()
        val preview = getUrlPreview(GetUrlPreview.Params(url = requestedUrl)).getOrThrow()

        // The redirect is still followed to reach the content; only the reported url differs.
        assertThat(preview.url).isEqualTo(requestedUrl)
        assertThat(preview.title).isEqualTo("Page title")
    }

    @Test
    fun `WHEN the page is not UTF-8 THEN the encoding is taken from the document`() = runTest {
        val title = "Ação"
        val body = """
            <html><head><meta charset="ISO-8859-1"><title>$title</title></head><body></body></html>
        """.trimIndent()

        server.enqueue(
            MockResponse(code = 200)
                .newBuilder()
                .addHeader("Content-Type", "text/html")
                .body(Buffer().write(body.toByteArray(Charsets.ISO_8859_1)))
                .build(),
        )

        val preview = getUrlPreview(GetUrlPreview.Params(url = server.url("/post").toString())).getOrThrow()

        assertThat(preview.title).isEqualTo(title)
    }

    @Test
    fun `WHEN the response is an error THEN the preview falls back to the params`() = runTest {
        server.enqueue(MockResponse(code = 404))

        val preview = getUrlPreview(
            GetUrlPreview.Params(url = server.url("/post").toString(), title = "Fallback title"),
        ).getOrThrow()

        assertThat(preview.url).isEqualTo(server.url("/post").toString())
        assertThat(preview.title).isEqualTo("Fallback title")
    }

    @Test
    fun `WHEN neither preference is enabled THEN the page is not requested`() = runTest {
        every { mockUserRepository.autoFillDescription } returns false
        every { mockUserRepository.followRedirects } returns false

        val preview = getUrlPreview(
            GetUrlPreview.Params(url = server.url("/post").toString(), title = "Fallback title"),
        ).getOrThrow()

        assertThat(preview.title).isEqualTo("Fallback title")
        assertThat(server.requestCount).isEqualTo(0)
    }

    private fun html(title: String): String =
        "<html><head><title>$title</title></head><body></body></html>"

    private fun htmlResponse(body: String): MockResponse = MockResponse(code = 200)
        .newBuilder()
        .addHeader("Content-Type", "text/html")
        .body(body)
        .build()

    private fun redirectResponse(location: String): MockResponse = MockResponse(code = 302)
        .newBuilder()
        .addHeader("Location", location)
        .build()
}
