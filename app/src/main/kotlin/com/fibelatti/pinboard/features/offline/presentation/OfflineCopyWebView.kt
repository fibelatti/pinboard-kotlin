package com.fibelatti.pinboard.features.offline.presentation

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.ui.theme.ExtendedTheme
import java.io.File

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

    val webView: WebView = remember(localContext, file.parentFile) {
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

            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest,
                ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

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
            }
        }
    }

    val nestedScrollDirection by rememberScrollDirection(webView)
    val currentOnScrollDirectionChanged by rememberUpdatedState(onScrollDirectionChange)

    SideEffect(nestedScrollDirection) {
        currentOnScrollDirectionChanged(nestedScrollDirection)
    }

    SideEffect(offlineCopy.bookmarkId) {
        webView.loadUrl("https://$ASSET_LOADER_DOMAIN/$ASSET_PATH/${file.name}")
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.background(color = ExtendedTheme.colors.backgroundNoOverlay),
    )
}

private const val ASSET_LOADER_DOMAIN = "appassets.androidplatform.net"
private const val ASSET_PATH = "offline"
