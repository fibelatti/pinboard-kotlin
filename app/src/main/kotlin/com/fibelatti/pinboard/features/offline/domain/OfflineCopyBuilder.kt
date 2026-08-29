package com.fibelatti.pinboard.features.offline.domain

import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.coRunCatching
import com.fibelatti.core.platform.UserAgentProvider
import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Article
import net.dankito.readability4j.extended.Readability4JExtended
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber

/**
 * Turns a URL into a single, self-contained HTML document that can be rendered with no network
 * access.
 *
 * This generates an output where:
 * - **Nothing in the output may trigger a network request.** Images are embedded as `data:` URIs
 *   and every other external reference is removed. An offline copy that fetches anything is not
 *   offline.
 * - **The output is one file.** No sibling assets, so storing and deleting a copy is a single
 *   file operation.
 */
class OfflineCopyBuilder @Inject constructor(
    @RestApi(RestApiProvider.COMMON) private val httpClient: HttpClient,
    private val userAgentProvider: UserAgentProvider,
) {

    suspend fun build(url: String, fallbackTitle: String): Result<Output> = coRunCatching {
        val pageBytes: ByteArray = fetchPage(url)
        val document: Document = withContext(Dispatchers.IO) {
            // Parsed from bytes rather than a String so jsoup can sniff the encoding from the BOM
            // and the <meta charset> tag. Most pages omit the charset from the Content-Type header,
            // and Ktor would then assume UTF-8 and mangle every non-UTF-8 site.
            Jsoup.parse(/* in = */ pageBytes.inputStream(), /* charsetName = */ null, /* baseUri = */ url)
        }

        val article: Article = Readability4JExtended(uri = url, document = document.withResolvedImageUrls()).parse()

        // Readability never returns null for a page it can't make sense of — it returns a wrapper
        // holding little more than the page <title>. Saving that would give the user a copy that
        // opens onto nothing, so treat too-short content as a failure instead.
        val content: Element = article.articleContent
            ?.takeIf { it.text().length >= MIN_CONTENT_CHARS }
            ?: return Result.failure(NoReadableContentException(url))

        val truncated: Boolean = inlineImages(content = content, pageUrl = url)

        Output(
            html = render(
                title = article.title.ifNullOrBlank { fallbackTitle },
                byline = article.byline,
                url = url,
                content = content,
            ),
            truncated = truncated,
        )
    }

    private suspend fun fetchPage(url: String): ByteArray {
        val response: HttpResponse = httpClient.get(urlString = url) {
            offlineCaptureHeaders()
        }

        if (!response.status.isSuccess()) {
            throw OfflineCaptureFailedException(url = url, status = response.status.value)
        }

        if ((response.contentLength() ?: 0L) > MAX_PAGE_BYTES) {
            throw OfflineCaptureFailedException(url = url, status = null)
        }

        return response.bodyAsBytes().also { bytes ->
            if (bytes.size > MAX_PAGE_BYTES) {
                throw OfflineCaptureFailedException(url = url, status = null)
            }
        }
    }

    /**
     * The shared client installs an [io.ktor.client.plugins.cache.HttpCache] backed by `cacheDir`.
     * Without this, every captured page and image would be written a second time into the HTTP
     * cache, doubling the disk cost.
     */
    private fun HttpRequestBuilder.offlineCaptureHeaders() {
        header(HttpHeaders.UserAgent, userAgentProvider.userAgent)
        header(HttpHeaders.CacheControl, "no-store")
    }

    /**
     * Makes every image URL absolute before Readability sees the document.
     *
     * Readability4J resolves relative URLs itself, but its implementation drops the port, turning
     * `http://host:8080/a.png` into `http://host/a.png`, which then fails to load for anything not
     * served on the default port. Jsoup's [absUrl] gets it right, and an already-absolute URL
     * passes through Readability unmodified.
     *
     * The responsive attributes are dropped here for the same reason: Readability's lazy-image
     * handling reads them and can put a relative URL back into `src`.
     */
    private fun Document.withResolvedImageUrls(): Document = apply {
        select("img").forEach { image: Element ->
            val resolved: String = image.absUrl("src")
            if (resolved.isNotBlank()) {
                image.attr("src", resolved)
            }
            image.removeAttr("srcset")
            image.removeAttr("data-srcset")
        }
    }

    /**
     * Replaces every remote image with an inlined `data:` URI, and drops the ones that can't be
     * inlined so the rendered copy never reaches out to the network.
     *
     * The caps exist because a copy is meant to be a lightweight article, not a mirror of the page:
     * an unbounded inline pass on an image-heavy page would produce a file several times larger
     * than the page it came from.
     */
    private suspend fun inlineImages(content: Element, pageUrl: String): Boolean {
        var truncated = false
        var totalBytes = 0L
        var inlined = 0

        for (image: Element in content.select("img")) {
            // Readability rewrites `src` while handling lazy-loaded images, so what is left is not
            // reliably absolute. Resolve it here rather than trusting the library to have done it.
            val source: String = image.attr("src").resolvedAgainst(pageUrl)

            // Responsive attributes point at URLs that are not inlined below, so they have to go
            // or the renderer would prefer them over the inlined `src` and hit the network.
            image.removeAttr("srcset")
            image.removeAttr("data-srcset")
            image.removeAttr("sizes")
            image.removeAttr("loading")

            if (source.startsWith(DATA_URI_PREFIX)) continue

            if (inlined >= MAX_IMAGES || totalBytes >= MAX_TOTAL_IMAGE_BYTES || !source.startsWith("http")) {
                truncated = true
                image.remove()
                continue
            }

            val remoteImage: RemoteImage? = fetchImage(source)

            if (remoteImage == null || remoteImage.bytes.size > MAX_IMAGE_BYTES) {
                truncated = true
                image.remove()
                continue
            }

            image.attr("src", remoteImage.bytes.asDataUri(mimeType = remoteImage.mimeType))
            totalBytes += remoteImage.bytes.size
            inlined++
        }

        // Remove anything left that could still hit the network on render.
        content.select("script, iframe, object, embed, video, audio, source, link, style, noscript")
            .remove()
        content.select("[style]").forEach { it.removeAttr("style") }

        return truncated
    }

    private suspend fun fetchImage(url: String): RemoteImage? = coRunCatching {
        val response: HttpResponse = httpClient.get(urlString = url) {
            offlineCaptureHeaders()
        }

        if (!response.status.isSuccess()) return@coRunCatching null

        val bytes: ByteArray = response.bodyAsBytes().takeIf { it.isNotEmpty() } ?: return@coRunCatching null

        RemoteImage(bytes = bytes, mimeType = response.imageMimeType(url))
    }.recover { throwable ->
        // A single unreachable image must not fail the whole capture.
        Timber.d(throwable, "Failed to inline image: %s", url)
        null
    }.getOrNull()

    /**
     * The served `Content-Type`, which is the only authoritative answer, falling back to the URL
     * when the header is missing or is not an image type — hotlink-blocked images are commonly
     * answered with an HTML error page, and the extension is the better guess in that case.
     */
    private fun HttpResponse.imageMimeType(url: String): String {
        val contentType: ContentType? = contentType()

        return if (contentType != null && contentType.contentType.equals(other = "image", ignoreCase = true)) {
            contentType.withoutParameters().toString()
        } else {
            mimeTypeOf(url)
        }
    }

    private fun String.resolvedAgainst(pageUrl: String): String = when {
        isBlank() -> ""
        startsWith(DATA_URI_PREFIX) || startsWith("http") -> this
        else -> runCatching { URI(pageUrl).resolve(this).toString() }.getOrDefault("")
    }

    private fun ByteArray.asDataUri(mimeType: String): String {
        return "$DATA_URI_PREFIX$mimeType;base64,${kotlin.io.encoding.Base64.encode(this)}"
    }

    private fun mimeTypeOf(url: String): String {
        // Cache busters and resizing parameters are common on image URLs, and the extension sits
        // before them rather than at the end of the string.
        val path: String = url.substringBefore('?').substringBefore('#')

        return when {
            path.endsWith(".png", ignoreCase = true) -> "image/png"
            path.endsWith(".gif", ignoreCase = true) -> "image/gif"
            path.endsWith(".webp", ignoreCase = true) -> "image/webp"
            path.endsWith(".svg", ignoreCase = true) -> "image/svg+xml"
            else -> "image/jpeg"
        }
    }

    private fun render(title: String, byline: String?, url: String, content: Element): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            append("<html><head>")
            append("<meta charset=\"utf-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            append("<title>${title.escaped()}</title>")
            append("<style>$STYLES</style>")
            append("</head><body>")
            append("<header><h1>${title.escaped()}</h1>")
            if (!byline.isNullOrBlank()) {
                append("<p class=\"byline\">${byline.escaped()}</p>")
            }
            append("<p class=\"source\"><a href=\"${url.escaped()}\">")
            append(url.escaped())
            append("</a></p></header>")
            append("<main>${content.html()}</main>")
            append("</body></html>")
        }
    }

    private fun String.escaped(): String = replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    data class Output(
        val html: String,
        val truncated: Boolean,
    )

    /** Not a data class: [bytes] would give it an identity-based `equals`, and it is never compared. */
    private class RemoteImage(
        val bytes: ByteArray,
        val mimeType: String,
    )

    companion object {

        /**
         * Enough for a long-form article or a photo essay. Past this the page is a gallery, and
         * inlining all of it would produce a file out of proportion to the text it accompanies.
         */
        const val MAX_IMAGES: Int = 100

        /**
         * Covers a full-width hero image at modern display resolutions. Anything larger is usually
         * an unoptimized original that adds size without adding legibility on a phone.
         */
        const val MAX_IMAGE_BYTES: Int = 2 * 1024 * 1024

        /**
         * Decides how large a saved copy gets.
         *
         * Base64 inflates whatever is inlined by roughly a third, and the check below happens
         * before each fetch rather than after, so a copy can overshoot by one image: the effective
         * ceiling is about `(MAX_TOTAL_IMAGE_BYTES + MAX_IMAGE_BYTES) * 4 / 3`, or ~16 MB of HTML.
         * That is in the same range as a server-generated snapshot, which is the point at which a
         * reader-mode copy stops being the cheaper option.
         */
        const val MAX_TOTAL_IMAGE_BYTES: Long = 10L * 1024 * 1024

        /**
         * Below this, whatever Readability returned is boilerplate rather than an article (most
         * often just the page title). Low enough to keep genuinely short posts.
         */
        const val MIN_CONTENT_CHARS: Int = 200

        /**
         * Nothing otherwise stops a bookmark pointing at a huge document from being buffered whole.
         * Generous enough for script-heavy pages, whose markup dwarfs the article inside it, while
         * still refusing documents no phone should be asked to parse.
         */
        const val MAX_PAGE_BYTES: Int = 32 * 1024 * 1024

        private const val DATA_URI_PREFIX = "data:"

        private val STYLES: String = """
            :root { color-scheme: light dark; }
            body {
              margin: 0 auto; padding: 16px; max-width: 42rem;
              font-family: system-ui, sans-serif; font-size: 1rem; line-height: 1.6;
              overflow-wrap: break-word;
            }
            header { margin-bottom: 24px; }
            h1 { font-size: 1.5rem; line-height: 1.3; margin: 0 0 8px; }
            .byline, .source { font-size: 0.875rem; opacity: 0.7; margin: 4px 0; }
            img { max-width: 100%; height: auto; }
            pre { overflow-x: auto; }
            table { display: block; overflow-x: auto; }
            blockquote { margin-inline: 0; padding-inline-start: 16px; border-inline-start: 3px solid currentColor; opacity: 0.85; }
        """.trimIndent()
    }
}

class NoReadableContentException(url: String) : Exception("No readable content was found at $url")

class OfflineCaptureFailedException(url: String, status: Int?) : Exception(
    status?.let { "Failed to load $url: HTTP $it" } ?: "The page at $url is too large to save",
)
