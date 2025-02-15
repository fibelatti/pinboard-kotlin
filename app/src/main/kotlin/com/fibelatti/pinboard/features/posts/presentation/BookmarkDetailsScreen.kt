package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.LocalAppCompatActivity
import com.fibelatti.pinboard.core.android.composable.hiltActivityViewModel
import com.fibelatti.pinboard.core.android.isMultiPanelAvailable
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.rememberScrollDirection
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainBackNavigationEffect
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun BookmarkDetailsScreen(
    appStateViewModel: AppStateViewModel = hiltActivityViewModel(),
    mainViewModel: MainViewModel = hiltActivityViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
) {
    Surface(
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val postDetailState by appStateViewModel.postDetailContent.collectAsStateWithLifecycle(null)
        val popularPostDetailState by appStateViewModel.popularPostDetailContent.collectAsStateWithLifecycle(null)
        val post by rememberUpdatedState(
            newValue = postDetailState?.post ?: popularPostDetailState?.post ?: return@Surface,
        )

        val postDetailsScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()
        val popularPostsScreenState by popularPostsViewModel.screenState.collectAsStateWithLifecycle()
        val isLoading = postDetailsScreenState.isLoading || popularPostsScreenState.isLoading

        val isConnected = postDetailState?.isConnected ?: popularPostDetailState?.isConnected ?: false

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
    mainViewModel: MainViewModel = hiltActivityViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
) {
    val actionId = remember { UUID.randomUUID().toString() }

    LaunchedAppStateViewModelEffect(actionId = actionId)
    LaunchedMainViewModelEffect(actionId = actionId)
    LaunchedPostDetailViewModelEffect()
    LaunchedPopularPostsViewModelEffect()

    MainBackNavigationEffect(actionId = actionId)

    LaunchedErrorHandlerEffect(viewModel = postDetailViewModel)
    LaunchedErrorHandlerEffect(viewModel = popularPostsViewModel)

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    actionButton = if (currentState.actionButton.id == actionId) {
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
private fun LaunchedAppStateViewModelEffect(
    appStateViewModel: AppStateViewModel = hiltActivityViewModel(),
    mainViewModel: MainViewModel = hiltActivityViewModel(),
    actionId: String,
) {
    val content by appStateViewModel.content.collectAsStateWithLifecycle()
    val isSidePanelAvailable = isMultiPanelAvailable()
    val localContext = LocalContext.current

    LaunchedEffect(content, isSidePanelAvailable) {
        val (post, menuItems) = when (val current = content) {
            is PostDetailContent -> current.post to listOf(
                MainState.MenuItemComponent.DeleteBookmark,
                MainState.MenuItemComponent.EditBookmark,
                MainState.MenuItemComponent.OpenInBrowser,
            )

            is PopularPostDetailContent -> current.post to listOf(
                MainState.MenuItemComponent.SaveBookmark,
                MainState.MenuItemComponent.OpenInBrowser,
            )

            else -> return@LaunchedEffect
        }

        mainViewModel.updateState { currentState ->
            val actionButtonState = if (post.readLater == true && !post.isFile()) {
                MainState.ActionButtonComponent.Visible(
                    id = actionId,
                    label = localContext.getString(R.string.hint_mark_as_read),
                    data = post,
                )
            } else {
                MainState.ActionButtonComponent.Gone
            }

            if (isSidePanelAvailable) {
                currentState.copy(
                    actionButton = actionButtonState,
                    sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                        id = actionId,
                        menuItems = listOf(
                            MainState.MenuItemComponent.ShareBookmark,
                            *menuItems.toTypedArray(),
                            MainState.MenuItemComponent.CloseSidePanel,
                        ),
                        data = post,
                    ),
                )
            } else {
                currentState.copy(
                    title = MainState.TitleComponent.Gone,
                    subtitle = MainState.TitleComponent.Gone,
                    navigation = MainState.NavigationComponent.Visible(actionId),
                    actionButton = actionButtonState,
                    bottomAppBar = MainState.BottomAppBarComponent.Visible(
                        id = actionId,
                        menuItems = menuItems,
                        navigationIcon = null,
                        data = post,
                    ),
                    floatingActionButton = MainState.FabComponent.Visible(
                        id = actionId,
                        icon = R.drawable.ic_share,
                        data = post,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LaunchedMainViewModelEffect(
    mainViewModel: MainViewModel = hiltActivityViewModel(),
    appStateViewModel: AppStateViewModel = hiltActivityViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    popularPostsViewModel: PopularPostsViewModel = hiltViewModel(),
    actionId: String,
) {
    val localContext = LocalContext.current
    val localActivity = LocalAppCompatActivity.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(Unit) {
        mainViewModel.actionButtonClicks(actionId)
            .onEach { data: Any? -> (data as? Post)?.let(postDetailViewModel::toggleReadLater) }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.menuItemClicks(actionId)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.ShareBookmark -> {
                        localContext.shareText(R.string.posts_share_title, post.url)
                    }

                    is MainState.MenuItemComponent.DeleteBookmark -> showDeleteConfirmationDialog(localContext) {
                        postDetailViewModel.deletePost(post)
                    }

                    is MainState.MenuItemComponent.EditBookmark -> appStateViewModel.runAction(EditPost(post))
                    is MainState.MenuItemComponent.SaveBookmark -> popularPostsViewModel.saveLink(post)
                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(localContext, post)
                    is MainState.MenuItemComponent.CloseSidePanel -> localActivity.navigateBack()
                    else -> Unit
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.fabClicks(actionId)
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
    mainViewModel: MainViewModel = hiltActivityViewModel(),
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
        setDataAndType(Uri.parse(post.url), mimeType)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(newIntent)
    } catch (_: ActivityNotFoundException) {
        openUrlInExternalBrowser(context, post)
    }
}

fun openUrlInExternalBrowser(context: Context, post: Post) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.url)))
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
) {
    var hasError by remember { mutableStateOf(false) }

    when {
        post.isFile() -> {
            BookmarkPlaceholder(
                title = post.displayTitle,
                url = post.url,
                onButtonClicked = onOpenInFileViewerClicked,
                icon = painterResource(id = R.drawable.ic_mobile),
                description = stringResource(id = R.string.posts_open_with_file_viewer_description),
                buttonText = stringResource(id = R.string.posts_open_with_file_viewer),
            )
        }

        !isConnected -> {
            BookmarkPlaceholder(
                title = post.displayTitle,
                url = post.url,
                onButtonClicked = onOpenInBrowserClicked,
                description = stringResource(id = R.string.posts_url_offline_error),
            )
        }

        hasError -> {
            BookmarkPlaceholder(
                title = post.displayTitle,
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

                val nestedScrollDirection by webView.rememberScrollDirection()
                val currentOnScrollDirectionChanged by rememberUpdatedState(onScrollDirectionChanged)

                LaunchedEffect(nestedScrollDirection) {
                    currentOnScrollDirectionChanged(nestedScrollDirection)
                }

                DisposableEffect(webView) {
                    onDispose {
                        webView.stopLoading()
                        webView.destroy()
                    }
                }

                AndroidView(
                    factory = {
                        webView.apply {
                            if (url != post.url) loadUrl(post.url)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ExtendedTheme.colors.backgroundNoOverlay),
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
private fun BookmarkPlaceholder(
    title: String,
    url: String,
    onButtonClicked: () -> Unit,
    icon: Painter = painterResource(id = R.drawable.ic_browser),
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

        ElevatedButton(
            onClick = onButtonClicked,
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
