package com.fibelatti.pinboard.features.posts.presentation

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun BookmarkDetailsScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
    onOpenInFileViewerClicked: (Post) -> Unit,
    onOpenInBrowserClicked: (Post) -> Unit,
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val postDetailState by appStateViewModel.postDetailContent.collectAsStateWithLifecycle(null)
        val popularPostDetailState by appStateViewModel.postDetailContent.collectAsStateWithLifecycle(null)
        val post = postDetailState?.post ?: popularPostDetailState?.post ?: return@Surface

        val isDetailLoading by postDetailViewModel.loading.collectAsStateWithLifecycle(initialValue = false)
        val isPopularLoading by popularPostsViewModel.loading.collectAsStateWithLifecycle(initialValue = false)
        val isLoading = isDetailLoading || isPopularLoading

        val isConnected = postDetailState?.isConnected ?: popularPostDetailState?.isConnected ?: false

        BookmarkDetailsScreen(
            post = post,
            isLoading = isLoading,
            isConnected = isConnected,
            onOpenInFileViewerClicked = { onOpenInFileViewerClicked(post) },
            onOpenInBrowserClicked = { onOpenInBrowserClicked(post) },
        )
    }
}

@Composable
fun BookmarkDetailsScreen(
    post: Post,
    isLoading: Boolean = false,
    isConnected: Boolean = true,
    onOpenInFileViewerClicked: () -> Unit,
    onOpenInBrowserClicked: () -> Unit,
) {
    var hasError by remember { mutableStateOf(false) }

    when {
        post.isFile() -> {
            FileBookmark(
                title = post.title,
                url = post.url,
                onOpenInFileViewerClicked = onOpenInFileViewerClicked,
            )
        }

        !isConnected -> {
            BookmarkError(
                title = post.title,
                url = post.url,
                onButtonClicked = onOpenInBrowserClicked,
                description = stringResource(id = R.string.posts_url_offline_error),
            )
        }

        hasError -> {
            BookmarkError(
                title = post.title,
                url = post.url,
                onButtonClicked = onOpenInBrowserClicked,
            )
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                var initialWebViewLoading by remember { mutableStateOf(true) }

                val localContext = LocalContext.current
                val webView: WebView = remember(localContext) {
                    WebView(localContext).apply {
                        webViewClient = object : WebViewClient() {

                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (initialWebViewLoading) {
                                    initialWebViewLoading = false
                                    hasError = false
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                hasError = true
                            }
                        }
                    }
                }

                DisposableEffect(webView) {
                    onDispose {
                        webView.stopLoading()
                        webView.destroy()
                    }
                }

                AndroidView(
                    factory = { context -> NestedScrollView(context).apply { addView(webView) } },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ExtendedTheme.colors.backgroundNoOverlay)
                        .nestedScroll(rememberNestedScrollInteropConnection()),
                    update = { if (webView.url != post.url) webView.loadUrl(post.url) },
                )

                AnimatedVisibility(
                    visible = isLoading || initialWebViewLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = ExtendedTheme.colors.backgroundNoOverlay),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileBookmark(
    title: String,
    url: String,
    onOpenInFileViewerClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_file),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )

        Text(
            text = url,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        ElevatedButton(
            onClick = onOpenInFileViewerClicked,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(text = stringResource(id = R.string.posts_open_with_file_viewer))
        }
    }
}

@Composable
private fun BookmarkError(
    title: String,
    url: String,
    onButtonClicked: () -> Unit,
    description: String = stringResource(id = R.string.posts_url_error),
    buttonText: String = stringResource(id = R.string.posts_open_in_browser),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_warning),
            contentDescription = stringResource(id = R.string.cd_warning),
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )

        Text(
            text = url,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = description,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        ElevatedButton(
            onClick = onButtonClicked,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(text = buttonText)
        }
    }
}

@Composable
@ThemePreviews
private fun FileBookmarkPreview() {
    ExtendedTheme {
        FileBookmark(
            title = "Some bookmark",
            url = "https://www.bookmark.com",
            onOpenInFileViewerClicked = {},
        )
    }
}

@Composable
@ThemePreviews
private fun BookmarkErrorPreview() {
    ExtendedTheme {
        BookmarkError(
            title = "Some bookmark",
            url = "https://www.bookmark.com",
            onButtonClicked = {},
        )
    }
}
