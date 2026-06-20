package com.fibelatti.pinboard.features.posts.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.composable.ErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.SettingToggle
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Done
import com.fibelatti.pinboard.core.android.icons.Save
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.materialAlertDialogBuilder
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagManager
import com.fibelatti.ui.foundation.rememberKeyboardState
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun EditBookmarkScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    val appState by mainViewModel.appState.collectAsStateWithLifecycle()
    val postState by editPostViewModel.postState.collectAsStateWithLifecycle(initialValue = null)
    val currentState by rememberUpdatedState(newValue = postState ?: return)

    val editPostScreenState by editPostViewModel.screenState.collectAsStateWithLifecycle()
    val postDetailScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()
    val tagManagerState by editPostViewModel.tagManagerState.collectAsStateWithLifecycle(TagManagerState())

    LaunchedViewModelEffects()

    EditBookmarkScreen(
        appMode = appState.appMode,
        post = currentState,
        isNewBookmark = editPostScreenState.isNewBookmark,
        isLoading = editPostScreenState.isLoading || postDetailScreenState.isLoading,
        onUrlChange = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(url = newValue) }
        },
        urlError = editPostScreenState.invalidUrlError,
        onTitleChange = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(title = newValue) }
        },
        titleError = editPostScreenState.invalidTitleError,
        onDescriptionChange = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(description = newValue) }
        },
        onNotesChange = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(notes = newValue) }
        },
        onPrivateChange = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(private = newValue) }
        },
        onReadLaterChange = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(readLater = newValue) }
        },
        searchTagInput = tagManagerState.currentQuery,
        onSearchTagInputChange = editPostViewModel::setTagSearchQuery,
        onAddTagClick = editPostViewModel::addTag,
        suggestedTags = tagManagerState.suggestedTags,
        onSuggestedTagClick = editPostViewModel::addTag,
        currentTagsTitle = stringResource(id = tagManagerState.displayTitle),
        currentTags = tagManagerState.tags,
        onRemoveCurrentTagClick = editPostViewModel::removeTag,
    )
}

// region ViewModel setup
@Composable
private fun LaunchedViewModelEffects(
    mainViewModel: MainViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    val localContext = LocalContext.current
    val localResources = LocalResources.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val imeVisible by rememberKeyboardState()

    BackHandler {
        if (editPostViewModel.hasPendingChanges()) {
            localContext.materialAlertDialogBuilder().apply {
                setMessage(R.string.alert_confirm_unsaved_changes)
                setPositiveButton(R.string.hint_yes) { _, _ -> mainViewModel.runAction(NavigateBack) }
                setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
            }.applySecureFlag().show()
        } else {
            mainViewModel.runAction(NavigateBack)
        }
    }

    SideEffect(imeVisible) {
        if (imeVisible) {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    actionButton = MainState.ActionButtonComponent.Visible(
                        contentType = EditPostContent::class,
                        icon = AppIcons.Save,
                        label = localResources.getString(R.string.hint_save),
                    ),
                )
            }
        } else {
            mainViewModel.updateState { currentState ->
                currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
            }
        }
    }

    LaunchedMainViewModelEffect()
    LaunchedEditPostViewModelEffect()
    LaunchedPostDetailViewModelEffect()

    val editError by editPostViewModel.error.collectAsStateWithLifecycle()
    ErrorHandlerEffect(
        error = editError,
        handler = editPostViewModel::errorHandled,
        postAction = {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    floatingActionButton = MainState.FabComponent.Visible(
                        contentType = EditPostContent::class,
                        icon = AppIcons.Done,
                    ),
                )
            }
        },
    )

    val detailError by postDetailViewModel.error.collectAsStateWithLifecycle()
    ErrorHandlerEffect(error = detailError, handler = postDetailViewModel::errorHandled)

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    actionButton = if (currentState.actionButton.contentType == EditPostContent::class) {
                        MainState.ActionButtonComponent.Gone
                    } else {
                        currentState.actionButton
                    },
                )
            }

            keyboardController?.hide()
        }
    }
}

@Composable
private fun LaunchedMainViewModelEffect(
    mainViewModel: MainViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
) {
    val localContext = LocalContext.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        mainViewModel.actionButtonClicks(contentType = EditPostContent::class)
            .onEach {
                keyboardController?.hide()
                editPostViewModel.saveLink()
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.menuItemClicks(contentType = EditPostContent::class)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.DeleteBookmark -> showDeleteConfirmationDialog(localContext) {
                        postDetailViewModel.deletePost(post)
                    }

                    is MainState.MenuItemComponent.ToggleArchived -> postDetailViewModel.toggleArchived(post)

                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(localContext, post)

                    else -> Unit
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.fabClicks(contentType = EditPostContent::class)
            .onEach {
                keyboardController?.hide()
                editPostViewModel.saveLink()
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}

@Composable
private fun LaunchedEditPostViewModelEffect(
    mainViewModel: MainViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
) {
    val screenState by editPostViewModel.screenState.collectAsStateWithLifecycle()
    val localView = LocalView.current

    SideEffect(screenState) {
        when {
            screenState.saved -> {
                localView.showBanner(R.string.posts_saved_feedback)
                editPostViewModel.userNotified()
            }

            screenState.invalidUrlError.isNotEmpty() || screenState.invalidTitleError.isNotEmpty() -> {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        floatingActionButton = MainState.FabComponent.Visible(
                            contentType = EditPostContent::class,
                            icon = AppIcons.Done,
                        ),
                    )
                }
            }

            screenState.isLoading -> {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        actionButton = MainState.ActionButtonComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchedPostDetailViewModelEffect(
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    val screenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

    val localView = LocalView.current

    SideEffect(screenState) {
        val current = screenState
        if (current.deleted is Success<Boolean> && current.deleted.value) {
            localView.showBanner(R.string.posts_deleted_feedback)
            postDetailViewModel.userNotified()
        }
    }
}
// endregion ViewModel setup

// region Content
@Composable
private fun EditBookmarkScreen(
    appMode: AppMode,
    post: Post,
    isNewBookmark: Boolean,
    isLoading: Boolean,
    onUrlChange: (String) -> Unit,
    urlError: String,
    onTitleChange: (String) -> Unit,
    titleError: String,
    onDescriptionChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onPrivateChange: (Boolean) -> Unit,
    onReadLaterChange: (Boolean) -> Unit,
    searchTagInput: String,
    onSearchTagInputChange: (String) -> Unit,
    onAddTagClick: (String) -> Unit,
    suggestedTags: List<String>,
    onSuggestedTagClick: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: List<Tag>,
    onRemoveCurrentTagClick: (Tag) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExtendedTheme.colors.backgroundNoOverlay),
    ) {
        BookmarkContent(
            appMode = appMode,
            post = post,
            isNewBookmark = isNewBookmark,
            onUrlChange = onUrlChange,
            urlError = urlError,
            onTitleChange = onTitleChange,
            titleError = titleError,
            onDescriptionChange = onDescriptionChange,
            onNotesChange = onNotesChange,
            onPrivateChange = onPrivateChange,
            onReadLaterChange = onReadLaterChange,
            searchTagInput = searchTagInput,
            onSearchTagInputChange = onSearchTagInputChange,
            onAddTagClick = onAddTagClick,
            suggestedTags = suggestedTags,
            onSuggestedTagClick = onSuggestedTagClick,
            currentTagsTitle = currentTagsTitle,
            currentTags = currentTags,
            onRemoveCurrentTagClick = onRemoveCurrentTagClick,
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = ExtendedTheme.colors.backgroundNoOverlay)
                    .testTag(tag = "editor-loading-indicator"),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BookmarkContent(
    appMode: AppMode,
    post: Post,
    isNewBookmark: Boolean,
    onUrlChange: (String) -> Unit,
    urlError: String,
    onTitleChange: (String) -> Unit,
    titleError: String,
    onDescriptionChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onPrivateChange: (Boolean) -> Unit,
    onReadLaterChange: (Boolean) -> Unit,
    searchTagInput: String,
    onSearchTagInputChange: (String) -> Unit,
    onAddTagClick: (String) -> Unit,
    suggestedTags: List<String>,
    onSuggestedTagClick: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: List<Tag>,
    onRemoveCurrentTagClick: (Tag) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
    ) {
        if (post.pendingSync != null) {
            PendingSyncIndicator(
                text = when (post.pendingSync) {
                    PendingSync.ADD -> stringResource(id = R.string.posts_pending_add_expanded)
                    PendingSync.UPDATE -> stringResource(id = R.string.posts_pending_update_expanded)
                    PendingSync.DELETE -> stringResource(id = R.string.posts_pending_delete_expanded)
                    PendingSync.ARCHIVE -> stringResource(id = R.string.posts_pending_archive_expanded)
                    PendingSync.UNARCHIVE -> stringResource(id = R.string.posts_pending_unarchive_expanded)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        BookmarkBasicDetails(
            appMode = appMode,
            isNewBookmark = isNewBookmark,
            url = post.url,
            onUrlChange = onUrlChange,
            urlError = urlError,
            title = post.displayTitle,
            onTitleChange = onTitleChange,
            titleError = titleError,
            description = post.displayDescription,
            onDescriptionChange = onDescriptionChange,
            notes = post.notes.orEmpty(),
            onNotesChange = onNotesChange,
        )

        BookmarkFlags(
            appMode = appMode,
            private = post.private,
            onPrivateChange = onPrivateChange,
            readLater = post.readLater,
            onReadLaterChange = onReadLaterChange,
        )

        TagManager(
            searchTagInput = searchTagInput,
            onSearchTagInputChange = onSearchTagInputChange,
            onAddTagClick = onAddTagClick,
            suggestedTags = suggestedTags,
            onSuggestedTagClick = onSuggestedTagClick,
            currentTagsTitle = currentTagsTitle,
            currentTags = currentTags,
            onRemoveCurrentTagClick = onRemoveCurrentTagClick,
            modifier = Modifier.padding(bottom = 100.dp),
        )
    }
}

@Composable
private fun BookmarkBasicDetails(
    appMode: AppMode,
    isNewBookmark: Boolean,
    url: String,
    onUrlChange: (String) -> Unit,
    urlError: String,
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String,
    description: String,
    onDescriptionChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }

        val urlFieldState = rememberTextFieldState(initialText = url)
        val urlSanitizationRegex = remember { "\\s".toRegex() }

        // The Pinboard API uses the URL as the key; Changing the URL means creating a new bookmark
        val isUrlInputEnabled = isNewBookmark || appMode != AppMode.PINBOARD

        LaunchedEffect(Unit) {
            if (isUrlInputEnabled) {
                // Compose bug: without this delay the cursor won't appear
                delay(timeMillis = 100)
                focusRequester.requestFocus()
            }
        }

        SideEffect(urlFieldState.text) {
            onUrlChange(urlFieldState.text.toString())
        }

        OutlinedTextField(
            state = urlFieldState,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            enabled = isUrlInputEnabled,
            label = { Text(text = stringResource(id = R.string.posts_add_url)) },
            supportingText = {
                if (urlError.isNotEmpty()) {
                    Text(text = urlError)
                } else if (!isUrlInputEnabled) {
                    Text(
                        text = stringResource(R.string.posts_add_non_editable_url),
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            },
            isError = urlError.isNotEmpty(),
            inputTransformation = InputTransformation.byValue { _, proposed ->
                proposed.replace(urlSanitizationRegex, "")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            onKeyboardAction = KeyboardActionHandler { focusManager.moveFocus(FocusDirection.Next) },
            lineLimits = TextFieldLineLimits.SingleLine,
            contentPadding = OutlinedTextFieldDefaults.contentPaddingWithLabel(
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
            ),
        )

        val titleFieldState = rememberTextFieldState(initialText = title)

        SideEffect(titleFieldState.text) {
            onTitleChange(titleFieldState.text.toString())
        }

        OutlinedTextField(
            state = titleFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.posts_add_url_title)) },
            supportingText = {
                if (titleError.isNotEmpty()) {
                    Text(text = titleError)
                }
            },
            isError = titleError.isNotEmpty(),
            inputTransformation = InputTransformation.maxLength(AppConfig.PinboardApiMaxLength.TEXT_TYPE.value),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            onKeyboardAction = KeyboardActionHandler { focusManager.moveFocus(FocusDirection.Next) },
            contentPadding = OutlinedTextFieldDefaults.contentPaddingWithLabel(
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
            ),
        )

        val descriptionFieldState = rememberTextFieldState(initialText = description)

        SideEffect(descriptionFieldState.text) {
            onDescriptionChange(descriptionFieldState.text.toString())
        }

        OutlinedTextField(
            state = descriptionFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.posts_add_url_description)) },
            supportingText = {},
            contentPadding = OutlinedTextFieldDefaults.contentPaddingWithLabel(
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
            ),
        )

        if (AppMode.LINKDING == appMode) {
            val notesFieldState = rememberTextFieldState(initialText = notes)

            SideEffect(notesFieldState.text) {
                onNotesChange(notesFieldState.text.toString())
            }

            OutlinedTextField(
                state = notesFieldState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.posts_add_url_notes)) },
                contentPadding = OutlinedTextFieldDefaults.contentPaddingWithLabel(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp,
                ),
            )
        }
    }
}

@Composable
private fun BookmarkFlags(
    appMode: AppMode,
    private: Boolean?,
    onPrivateChange: (Boolean) -> Unit,
    readLater: Boolean?,
    onReadLaterChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (AppMode.NO_API != appMode) {
            SettingToggle(
                title = stringResource(id = R.string.posts_add_private),
                description = null,
                checked = private == true,
                onCheckedChange = onPrivateChange,
                modifier = Modifier.weight(0.5f),
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.posts_add_read_later),
            description = null,
            checked = readLater == true,
            onCheckedChange = onReadLaterChange,
            modifier = Modifier.weight(0.5f),
        )
    }
}
// endregion Content

// region Previews
@Composable
@PreviewAll
private fun EditBookmarkScreenPreview(
    @PreviewParameter(provider = PostProvider::class) post: Post,
) {
    ExtendedTheme {
        EditBookmarkScreen(
            appMode = AppMode.PINBOARD,
            post = post.copy(description = post.description.take(200)),
            isNewBookmark = true,
            isLoading = false,
            onUrlChange = {},
            urlError = "",
            onTitleChange = {},
            titleError = "",
            onDescriptionChange = {},
            onNotesChange = {},
            onPrivateChange = {},
            onReadLaterChange = {},
            searchTagInput = "",
            onSearchTagInputChange = {},
            onAddTagClick = {},
            suggestedTags = emptyList(),
            onSuggestedTagClick = {},
            currentTagsTitle = stringResource(id = R.string.tags_added_title),
            currentTags = post.tags.orEmpty(),
            onRemoveCurrentTagClick = {},
        )
    }
}
// endregion Previews
