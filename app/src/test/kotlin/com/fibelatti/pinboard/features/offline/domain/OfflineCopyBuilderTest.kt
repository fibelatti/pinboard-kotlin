package com.fibelatti.pinboard.features.offline.domain

import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OfflineCopyBuilderTest {

    private lateinit var server: MockWebServer

    private val builder: OfflineCopyBuilder
        get() = OfflineCopyBuilder(
            httpClient = HttpClient(OkHttp),
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
    fun `WHEN the page has an article THEN a self-contained copy is produced`() = runTest {
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/photo.png")))
        server.enqueue(imageResponse(bytes = PNG_BYTES))

        val result = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")

        val output = result.getOrThrow()

        assertThat(output.truncated).isFalse()
        assertThat(output.html).contains("The first paragraph needs to be reasonably long")
        assertThat(output.html).contains("data:image/png;base64,")
        assertNothingRemote(output.html)
    }

    @Test
    fun `WHEN an image exceeds the size cap THEN it is dropped and the copy is marked truncated`() = runTest {
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/huge.png")))
        server.enqueue(imageResponse(bytes = ByteArray(OfflineCopyBuilder.MAX_IMAGE_BYTES + 1)))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(output.truncated).isTrue()
        assertThat(output.html).doesNotContain("data:image")
        assertThat(output.html).contains("The first paragraph needs to be reasonably long")
        assertNothingRemote(output.html)
    }

    @Test
    fun `WHEN an image cannot be fetched THEN the copy is still produced without it`() = runTest {
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/missing.png")))
        server.enqueue(MockResponse(code = 500))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(output.truncated).isTrue()
        assertThat(output.html).contains("The first paragraph needs to be reasonably long")
        assertNothingRemote(output.html)
    }

    @Test
    fun `WHEN the page has no readable content THEN it fails instead of saving an empty copy`() = runTest {
        server.enqueue(htmlResponse("<html><head><title>Nothing</title></head><body></body></html>"))

        val result = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(NoReadableContentException::class.java)
    }

    @Test
    fun `WHEN the page responds with an error status THEN the failure is returned`() = runTest {
        server.enqueue(MockResponse(code = 500))

        val result = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(OfflineCaptureFailedException::class.java)
    }

    @Test
    fun `WHEN the page is not UTF-8 THEN the declared charset is honoured`() = runTest {
        val body = """
            <html><head><meta charset="ISO-8859-1"><title>Olá</title></head><body><article>
            <p>Este é um parágrafo suficientemente longo para que o algoritmo de pontuação o
            considere conteúdo principal, com vírgulas, orações e texto bastante prolixo.</p>
            <p>Um segundo parágrafo substancial, com vírgulas e orações, para que o elemento que
            os contém seja tratado como o corpo do artigo e não como conteúdo descartável.</p>
            </article></body></html>
        """.trimIndent()

        server.enqueue(
            MockResponse(code = 200)
                .newBuilder()
                // No charset in the header: the only hint is the <meta> tag, which is the common
                // real-world case and the one Ktor's bodyAsText() would get wrong.
                .setHeader("Content-Type", "text/html")
                .body(Buffer().write(body.toByteArray(Charsets.ISO_8859_1)))
                .build(),
        )

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(output.html).contains("parágrafo")
        assertThat(output.html).doesNotContain("par�grafo")
    }

    /**
     * Readability4J resolves relative URLs itself but drops the port while doing it, so an image on
     * a host served off the default port would silently 404. The builder resolves URLs before
     * handing the document over; this pins that down, since MockWebServer always uses a random port
     * and would be the first thing to break if the pre-resolution step were removed.
     */
    @Test
    fun `WHEN the page is served on a non-default port THEN images are still inlined`() = runTest {
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/photo.png")))
        server.enqueue(imageResponse(bytes = PNG_BYTES))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(server.requestCount).isEqualTo(2)
        assertThat(output.html).contains("data:image/png;base64,")
        assertThat(output.truncated).isFalse()
    }

    @Test
    fun `WHEN the article has a title THEN it is used, otherwise the fallback`() = runTest {
        server.enqueue(htmlResponse(articleHtml(imageSrc = null)))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(output.html).contains("Page title from head")
    }

    @Test
    fun `WHEN the image URL has no usable extension THEN the served content type is used`() = runTest {
        // GIVEN a URL whose extension sits behind a resizing parameter, which is common enough that
        // guessing from the string alone would mislabel a large share of images
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/photo.png?w=800")))
        server.enqueue(imageResponse(bytes = PNG_BYTES, contentType = "image/webp"))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        // THEN the header is the only authoritative answer
        assertThat(output.html).contains("data:image/webp;base64,")
        assertThat(output.truncated).isFalse()
    }

    @Test
    fun `WHEN the content type carries a charset THEN it is dropped from the data URI`() = runTest {
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/photo.png")))
        server.enqueue(imageResponse(bytes = PNG_BYTES, contentType = "image/png; charset=binary"))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(output.html).contains("data:image/png;base64,")
        assertThat(output.html).doesNotContain("charset=binary")
    }

    @Test
    fun `WHEN the content type is not an image THEN the URL decides instead`() = runTest {
        // GIVEN hotlink-blocked images are commonly answered with an HTML error page carrying a 200,
        // and labelling that as text/html would leave the WebView with nothing it can render
        server.enqueue(htmlResponse(articleHtml(imageSrc = "/media/photo.gif")))
        server.enqueue(imageResponse(bytes = PNG_BYTES, contentType = "application/octet-stream"))

        val output = builder.build(url = server.url("/post").toString(), fallbackTitle = "Fallback")
            .getOrThrow()

        assertThat(output.html).contains("data:image/gif;base64,")
    }

    /**
     * The whole point of an offline copy: rendering it must not reach the network. Anything that
     * could issue a request (a remote src/href on a loadable element, a stylesheet, a script)
     * means the copy is not actually offline.
     */
    private fun assertNothingRemote(html: String) {
        assertThat(html).doesNotContain("<script")
        assertThat(html).doesNotContain("<iframe")
        assertThat(html).doesNotContain("<link")
        assertThat(html).doesNotContain("srcset")
        assertThat(html).doesNotContain("src=\"http")
        assertThat(html).doesNotContain("src='http")
    }

    private fun htmlResponse(body: String): MockResponse = MockResponse(code = 200)
        .newBuilder()
        .setHeader("Content-Type", "text/html; charset=utf-8")
        .body(body)
        .build()

    private fun imageResponse(
        bytes: ByteArray,
        contentType: String = "image/png",
    ): MockResponse = MockResponse(code = 200)
        .newBuilder()
        .setHeader("Content-Type", contentType)
        .body(Buffer().write(bytes))
        .build()

    private fun articleHtml(imageSrc: String?): String = """
        <!DOCTYPE html>
        <html>
          <head><title>Page title from head</title></head>
          <body>
            <nav id="site-nav"><a href="/">Home</a></nav>
            <article>
              <p>
                The first paragraph needs to be reasonably long, because Readability scores candidate
                nodes by text length and comma count, and a short paragraph would be discarded before
                it ever reaches the output. So this sentence rambles on a bit, deliberately.
              </p>
              <p>
                A second substantial paragraph, with commas, clauses, and enough prose that the
                scoring algorithm treats the containing element as the main article body rather than
                as boilerplate that should be stripped away entirely.
              </p>
              ${imageSrc?.let { "<img src=\"$it\" srcset=\"$it 2x\" alt=\"A photo\">" }.orEmpty()}
              <script>window.analytics.track('pageview');</script>
              <iframe src="https://ads.example.net/frame"></iframe>
            </article>
            <footer id="site-footer">Copyright example.com</footer>
          </body>
        </html>
    """.trimIndent()

    private companion object {

        /** A 1x1 transparent PNG. */
        val PNG_BYTES: ByteArray = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15.toByte(), 0xC4.toByte(),
            0x89.toByte(),
        )
    }
}
