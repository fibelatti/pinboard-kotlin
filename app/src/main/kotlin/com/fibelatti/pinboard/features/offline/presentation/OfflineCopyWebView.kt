package com.fibelatti.pinboard.features.offline.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.features.main.MainBottomAppBar
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.ui.theme.ExtendedTheme
import java.io.File
import java.io.InputStream
import java.io.SequenceInputStream
import kotlin.uuid.Uuid

/**
 * Renders a saved offline copy.
 *
 * The file is served over `https://appassets.androidplatform.net/...` by a [WebViewAssetLoader]
 * rather than loaded from a `file://` URL. A `file://` URL would require `allowFileAccess = true`,
 * which the platform documentation explicitly advises against, and would give the page a file
 * origin. This way the WebView keeps its default file access behavior.
 *
 * No JavaScript required since the saved copy is inert HTML.
 */
@Composable
fun OfflineCopyWebView(
    offlineCopy: OfflineCopy,
    file: File,
    onExternalLinkClick: (String) -> Unit,
    onScrollDirectionChange: (ScrollDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val localContext: Context = LocalContext.current

    var webViewRenderProcessId: String by remember { mutableStateOf(Uuid.random().toString()) }
    val webView: WebView = remember(localContext, webViewRenderProcessId, file.parentFile) {
        val assetLoader: WebViewAssetLoader = WebViewAssetLoader.Builder()
            .addPathHandler(
                "/$ASSET_PATH/",
                WebViewAssetLoader.InternalStoragePathHandler(
                    localContext,
                    requireNotNull(file.parentFile) { "The offline copy must live in a directory." },
                ),
            )
            .build()

        WebView(localContext).apply {
            settings.javaScriptEnabled = false
            settings.domStorageEnabled = false
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            @SuppressLint("MissingOnRenderProcessGone") // Already implemented, but lint still fails
            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest,
                ): WebResourceResponse? {
                    val response: WebResourceResponse = assetLoader.shouldInterceptRequest(request.url)
                        ?: return null

                    if (!response.mimeType.equals(other = MIME_TYPE_HTML, ignoreCase = true)) return response

                    return response.withBottomPadding(
                        cssPixels = request.url.getQueryParameter(QUERY_BOTTOM_PADDING)?.toIntOrNull() ?: 0,
                    )
                }

                /**
                 * Links inside a saved article and the header link back to the original still
                 * point at the live web. Following one in place would silently swap the offline
                 * copy for the real page while looking like the copy still worked, so anything
                 * that is not the copy itself is handed to the browser instead.
                 */
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean {
                    val url = request.url

                    return if (url.host == ASSET_LOADER_DOMAIN) {
                        false
                    } else {
                        onExternalLinkClick(url.toString())
                        true
                    }
                }

                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?,
                ): Boolean {
                    val parent: ViewGroup? = view?.parent as? ViewGroup
                    parent?.removeView(view)
                    view?.destroy()
                    webViewRenderProcessId = Uuid.random().toString()
                    return true
                }
            }
        }
    }

    val nestedScrollDirection by rememberScrollDirection(webView)
    val currentOnScrollDirectionChanged by rememberUpdatedState(onScrollDirectionChange)

    SideEffect(nestedScrollDirection) {
        currentOnScrollDirectionChanged(nestedScrollDirection)
    }

    // The bottom app bar is drawn over the content instead of taking space from it, so the end of a
    // page would sit underneath it.
    //
    // A CSS pixel is a density-independent pixel at the default zoom level.
    val bottomPadding: Int = with(LocalDensity.current) {
        WindowInsets.navigationBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Bottom)
            .add(WindowInsets(bottom = MainBottomAppBar.ContentClearance))
            .getBottom(this)
            .toDp()
            .value
            .toInt()
    }

    val url: String = remember(offlineCopy.bookmarkId, file, bottomPadding) {
        "https://$ASSET_LOADER_DOMAIN/$ASSET_PATH/${file.name}?$QUERY_BOTTOM_PADDING=$bottomPadding"
    }

    SideEffect(webViewRenderProcessId, url) {
        webView.loadUrl(url)
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    key(webViewRenderProcessId) {
        AndroidView(
            factory = { webView },
            modifier = modifier.background(color = ExtendedTheme.colors.backgroundNoOverlay),
        )
    }
}

/**
 * Appends a stylesheet padding the bottom of the document to the response.
 *
 * Trailing markup is reparented into `<body>` by the parser, so this needs no insertion point, and
 * being last it wins over the padding the document sets on itself.
 */
private fun WebResourceResponse.withBottomPadding(cssPixels: Int): WebResourceResponse {
    if (cssPixels <= 0) return this
    val body: InputStream = data ?: return this

    val style = "<style>body{padding-bottom:${cssPixels}px}</style>".toByteArray()

    return WebResourceResponse(
        /* mimeType = */ mimeType,
        /* encoding = */ encoding,
        /* data = */ SequenceInputStream(body, style.inputStream()),
    )
}

private const val ASSET_LOADER_DOMAIN = "appassets.androidplatform.net"
private const val ASSET_PATH = "offline"
private const val MIME_TYPE_HTML = "text/html"
private const val QUERY_BOTTOM_PADDING = "bottomPadding"
