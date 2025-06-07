@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun BookmarkDetailsScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState by mainViewModel.appState.collectAsStateWithLifecycle()

        val post by rememberUpdatedState(
            newValue = when (val current = appState.content) {
                is PostDetailContent -> current.post
                is PopularPostDetailContent -> current.post
                else -> return@Surface
            },
        )
        val isConnected by rememberUpdatedState(
            newValue = when (val current = appState.content) {
                is PostDetailContent -> current.isConnected
                is PopularPostDetailContent -> current.isConnected
                else -> false
            },
        )

        val postDetailsScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()
        val popularPostsScreenState by popularPostsViewModel.screenState.collectAsStateWithLifecycle()
        val isLoading by remember {
            derivedStateOf { postDetailsScreenState.isLoading || popularPostsScreenState.isLoading }
        }

        val localContext = LocalContext.current

        LaunchedViewModelEffects()

        BookmarkDetailsScreen(
            post = post,
            isLoading = isLoading,
            isConnected = isConnected,
            onOpenInFileViewerClicked = { openUrlInFileViewer(localContext, post) },
            onOpenInBrowserClicked = { openUrlInExternalBrowser(localContext, post) },
            onScrollDirectionChanged = mainViewModel::setCurrentScrollDirection,
        )
    }
}

// region ViewModel setup
@Composable
private fun LaunchedViewModelEffects(
    mainViewModel: MainViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
) {
    LaunchedMainViewModelEffect()
    LaunchedPostDetailViewModelEffect()
    LaunchedPopularPostsViewModelEffect()

    val detailError by postDetailViewModel.error.collectAsStateWithLifecycle()
    LaunchedErrorHandlerEffect(error = detailError, handler = postDetailViewModel::errorHandled)

    val popularError by popularPostsViewModel.error.collectAsStateWithLifecycle()
    LaunchedErrorHandlerEffect(error = popularError, handler = popularPostsViewModel::errorHandled)

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    actionButton = if (currentState.actionButton.contentType == PostDetailContent::class) {
                        MainState.ActionButtonComponent.Gone
                    } else {
                        currentState.actionButton
                    },
                )
            }
        }
    }
}

@Composable
private fun LaunchedMainViewModelEffect(
    mainViewModel: MainViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
) {
    val localContext = LocalContext.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle
    val localOnBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    LaunchedEffect(Unit) {
        mainViewModel.actionButtonClicks(contentType = PostDetailContent::class)
            .onEach { data: Any? -> (data as? Post)?.let(postDetailViewModel::toggleReadLater) }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.menuItemClicks(contentType = PostDetailContent::class)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.ShareBookmark -> {
                        localContext.shareText(R.string.posts_share_title, post.url)
                    }

                    is MainState.MenuItemComponent.DeleteBookmark -> showDeleteConfirmationDialog(localContext) {
                        postDetailViewModel.deletePost(post)
                    }

                    is MainState.MenuItemComponent.EditBookmark -> mainViewModel.runAction(EditPost(post))
                    is MainState.MenuItemComponent.SaveBookmark -> popularPostsViewModel.saveLink(post)
                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(localContext, post)
                    is MainState.MenuItemComponent.CloseSidePanel -> localOnBackPressedDispatcher?.onBackPressed()
                    else -> Unit
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.fabClicks(contentType = PostDetailContent::class)
            .onEach { data: Any? ->
                (data as? Post)?.let { localContext.shareText(R.string.posts_share_title, data.url) }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}

@Composable
private fun LaunchedPostDetailViewModelEffect(
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val screenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localView = LocalView.current

    LaunchedEffect(screenState) {
        val current = screenState
        when {
            current.deleted is Success<Boolean> && current.deleted.value -> {
                localView.showBanner(R.string.posts_deleted_feedback)
                postDetailViewModel.userNotified()
            }

            current.deleted is Failure -> {
                MaterialAlertDialogBuilder(localContext).apply {
                    setMessage(R.string.posts_deleted_error)
                    setPositiveButton(R.string.hint_ok) { dialog, _ -> dialog?.dismiss() }
                }.applySecureFlag().show()
            }

            current.updated is Success<Boolean> && current.updated.value -> {
                localView.showBanner(R.string.posts_marked_as_read_feedback)
                postDetailViewModel.userNotified()
                mainViewModel.updateState { currentState ->
                    currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                }
            }

            current.updated is Failure -> {
                localView.showBanner(R.string.posts_marked_as_read_error)
                postDetailViewModel.userNotified()
            }
        }
    }
}

@Composable
private fun LaunchedPopularPostsViewModelEffect(
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
) {
    val screenState by popularPostsViewModel.screenState.collectAsStateWithLifecycle()
    val localView = LocalView.current

    LaunchedEffect(screenState) {
        screenState.savedMessage?.let { messageRes ->
            localView.showBanner(messageRes)
            popularPostsViewModel.userNotified()
        }
    }
}
// endregion ViewModel setup

// region Service functions
fun showDeleteConfirmationDialog(context: Context, onConfirm: () -> Unit) {
    MaterialAlertDialogBuilder(context).apply {
        setMessage(R.string.alert_confirm_deletion)
        setPositiveButton(R.string.hint_yes) { _, _ -> onConfirm() }
        setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
    }.applySecureFlag().show()
}

private fun openUrlInFileViewer(context: Context, post: Post) {
    val mimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(post.url.substringAfterLast("."))
    val newIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(post.url.toUri(), mimeType)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(newIntent)
    } catch (_: ActivityNotFoundException) {
        openUrlInExternalBrowser(context, post)
    }
}

fun openUrlInExternalBrowser(context: Context, post: Post) {
    context.startActivity(Intent(Intent.ACTION_VIEW, post.url.toUri()))
}
// endregion Service functions

// region Content
@Composable
fun BookmarkDetailsScreen(
    post: Post,
    isLoading: Boolean,
    isConnected: Boolean,
    onOpenInFileViewerClicked: () -> Unit,
    onOpenInBrowserClicked: () -> Unit,
    onScrollDirectionChanged: (ScrollDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    var webViewLoadFailed by remember { mutableStateOf(false) }

    when {
        post.isFile() -> {
            BookmarkPlaceholder(
                title = post.displayTitle,
                url = post.url,
                onButtonClicked = onOpenInFileViewerClicked,
                icon = painterResource(id = R.drawable.ic_mobile),
                description = stringResource(id = R.string.posts_open_with_file_viewer_description),
                buttonText = stringResource(id = R.string.posts_open_with_file_viewer),
                modifier = modifier,
            )
        }

        !isConnected -> {
            BookmarkPlaceholder(
                title = post.displayTitle,
                url = post.url,
                onButtonClicked = onOpenInBrowserClicked,
                description = stringResource(id = R.string.posts_url_offline_error),
                modifier = modifier,
            )
        }

        webViewLoadFailed -> {
            BookmarkPlaceholder(
                title = post.displayTitle,
                url = post.url,
                onButtonClicked = onOpenInBrowserClicked,
                modifier = modifier,
            )
        }

        else -> {
            Box(
                modifier = modifier.fillMaxSize(),
            ) {
                var webViewLoading by remember { mutableStateOf(true) }

                val localContext = LocalContext.current
                val webView: WebView = remember(localContext) {
                    WebView(localContext).apply {
                        webViewClient = object : WebViewClient() {

                            override fun onPageFinished(view: WebView?, url: String?) {
                                webViewLoading = false
                                webViewLoadFailed = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                webViewLoadFailed = true
                            }
                        }
                    }
                }

                val nestedScrollDirection by rememberScrollDirection(webView)
                val currentOnScrollDirectionChanged by rememberUpdatedState(onScrollDirectionChanged)

                LaunchedEffect(nestedScrollDirection) {
                    currentOnScrollDirectionChanged(nestedScrollDirection)
                }

                LaunchedEffect(post.id) {
                    webView.loadUrl(post.url)
                    webViewLoading = true
                }

                DisposableEffect(webView) {
                    onDispose {
                        webView.stopLoading()
                        webView.destroy()
                    }
                }

                AndroidView(
                    factory = { webView },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ExtendedTheme.colors.backgroundNoOverlay),
                )

                AnimatedVisibility(
                    visible = isLoading || webViewLoading,
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
private fun BookmarkPlaceholder(
    title: String,
    url: String,
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(id = R.drawable.ic_browser),
    description: String = stringResource(id = R.string.posts_url_error),
    buttonText: String = stringResource(id = R.string.posts_open_in_browser),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = url,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = description,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(
            onClick = onButtonClicked,
            shapes = ExtendedTheme.defaultButtonShapes,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(text = buttonText)
        }
    }
}
// endregion Content

// region Previews
@Composable
@ThemePreviews
private fun FileBookmarkPreview() {
    ExtendedTheme {
        BookmarkPlaceholder(
            title = "Some bookmark",
            url = "https://www.bookmark.com",
            onButtonClicked = {},
            icon = painterResource(id = R.drawable.ic_mobile),
            description = stringResource(id = R.string.posts_open_with_file_viewer_description),
            buttonText = stringResource(id = R.string.posts_open_with_file_viewer),
        )
    }
}

@Composable
@ThemePreviews
private fun BookmarkErrorPreview() {
    ExtendedTheme {
        BookmarkPlaceholder(
            title = "Some bookmark",
            url = "https://www.bookmark.com",
            onButtonClicked = {},
        )
    }
}
// endregion Previews
