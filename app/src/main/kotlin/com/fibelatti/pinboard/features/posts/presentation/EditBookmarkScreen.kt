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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.android.extension.doOnApplyWindowInsets
import com.fibelatti.core.android.extension.hideKeyboard
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.composable.LaunchedErrorHandlerEffect
import com.fibelatti.pinboard.core.android.composable.SettingToggle
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagManager
import com.fibelatti.pinboard.features.tags.presentation.TagManagerViewModel
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun EditBookmarkScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    tagManagerViewModel: TagManagerViewModel = hiltViewModel(),
) {
    val appMode by appStateViewModel.appMode.collectAsStateWithLifecycle()
    val addPostContent by appStateViewModel.addPostContent.collectAsStateWithLifecycle(initialValue = null)
    val editPostContent by appStateViewModel.editPostContent.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(addPostContent) {
        addPostContent?.let {
            val emptyPost = Post(
                url = "",
                title = "",
                description = "",
                private = it.defaultPrivate,
                readLater = it.defaultReadLater,
                tags = it.defaultTags.ifEmpty { null },
            )

            editPostViewModel.initializePost(emptyPost)
            tagManagerViewModel.initializeTags(it.defaultTags)
        }
    }

    LaunchedEffect(editPostContent) {
        editPostContent?.let {
            editPostViewModel.initializePost(it.post)
            tagManagerViewModel.initializeTags(it.post.tags.orEmpty())
        }
    }

    val postState by editPostViewModel.postState.collectAsStateWithLifecycle(initialValue = null)
    val currentState by rememberUpdatedState(newValue = postState ?: return)

    val editPostScreenState by editPostViewModel.screenState.collectAsStateWithLifecycle()
    val postDetailScreenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

    val tagManagerState by tagManagerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(tagManagerState) {
        editPostViewModel.searchForTag(tagManagerState.currentQuery, tagManagerState.tags)
        editPostViewModel.updatePost { post -> post.copy(tags = tagManagerState.tags.ifEmpty { null }) }
    }

    LaunchedViewModelEffects()

    EditBookmarkScreen(
        appMode = appMode,
        post = currentState,
        isLoading = editPostScreenState.isLoading || postDetailScreenState.isLoading,
        onUrlChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(url = newValue) }
        },
        urlError = editPostScreenState.invalidUrlError,
        onTitleChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(title = newValue) }
        },
        titleError = editPostScreenState.invalidTitleError,
        onDescriptionChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(description = newValue) }
        },
        onNotesChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(notes = newValue) }
        },
        onPrivateChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(private = newValue) }
        },
        onReadLaterChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(readLater = newValue) }
        },
        searchTagInput = tagManagerState.currentQuery,
        onSearchTagInputChanged = tagManagerViewModel::setQuery,
        onAddTagClicked = tagManagerViewModel::addTag,
        suggestedTags = tagManagerState.suggestedTags,
        onSuggestedTagClicked = tagManagerViewModel::addTag,
        currentTagsTitle = stringResource(id = tagManagerState.displayTitle),
        currentTags = tagManagerState.tags,
        onRemoveCurrentTagClicked = tagManagerViewModel::removeTag,
    )
}

object EditBookmarkScreen {

    val ACTION_ID = UUID.randomUUID().toString()
}

// region ViewModel setup
@Composable
private fun LaunchedViewModelEffects(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    val localContext = LocalContext.current
    val localView = LocalView.current

    LaunchedEffect(Unit) {
        localView.doOnApplyWindowInsets { _, insets, _, _ ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        actionButton = MainState.ActionButtonComponent.Visible(
                            label = localContext.getString(R.string.hint_save),
                            id = EditBookmarkScreen.ACTION_ID,
                        ),
                    )
                }
            } else {
                mainViewModel.updateState { currentState ->
                    currentState.copy(actionButton = MainState.ActionButtonComponent.Gone)
                }
            }
        }
    }

    LaunchedMainViewModelEffect(actionId = EditBookmarkScreen.ACTION_ID)
    LaunchedEditPostViewModelEffect(actionId = EditBookmarkScreen.ACTION_ID)
    LaunchedPostDetailViewModelEffect()

    BackHandler {
        if (editPostViewModel.hasPendingChanges()) {
            MaterialAlertDialogBuilder(localContext).apply {
                setMessage(R.string.alert_confirm_unsaved_changes)
                setPositiveButton(R.string.hint_yes) { _, _ -> appStateViewModel.runAction(NavigateBack) }
                setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
            }.applySecureFlag().show()
        } else {
            appStateViewModel.runAction(NavigateBack)
        }
    }

    val editError by editPostViewModel.error.collectAsStateWithLifecycle()
    LaunchedErrorHandlerEffect(
        error = editError,
        handler = editPostViewModel::errorHandled,
        postAction = {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    floatingActionButton = MainState.FabComponent.Visible(
                        id = EditBookmarkScreen.ACTION_ID,
                        icon = R.drawable.ic_done,
                    ),
                )
            }
        },
    )

    val detailError by postDetailViewModel.error.collectAsStateWithLifecycle()
    LaunchedErrorHandlerEffect(error = detailError, handler = postDetailViewModel::errorHandled)

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.updateState { currentState ->
                currentState.copy(
                    actionButton = if (currentState.actionButton.id == EditBookmarkScreen.ACTION_ID) {
                        MainState.ActionButtonComponent.Gone
                    } else {
                        currentState.actionButton
                    },
                )
            }

            localView.hideKeyboard()
        }
    }
}

@Composable
private fun LaunchedMainViewModelEffect(
    actionId: String,
    mainViewModel: MainViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
) {
    val localContext = LocalContext.current
    val localView = LocalView.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(Unit) {
        mainViewModel.actionButtonClicks(actionId)
            .onEach {
                localView.hideKeyboard()
                editPostViewModel.saveLink()
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.menuItemClicks(actionId)
            .onEach { (menuItem, post) ->
                if (post !is Post) return@onEach
                when (menuItem) {
                    is MainState.MenuItemComponent.DeleteBookmark -> showDeleteConfirmationDialog(localContext) {
                        postDetailViewModel.deletePost(post)
                    }

                    is MainState.MenuItemComponent.OpenInBrowser -> openUrlInExternalBrowser(localContext, post)

                    else -> Unit
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
        mainViewModel.fabClicks(actionId)
            .onEach {
                localView.hideKeyboard()
                editPostViewModel.saveLink()
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}

@Composable
private fun LaunchedEditPostViewModelEffect(
    actionId: String,
    mainViewModel: MainViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
    tagManagerViewModel: TagManagerViewModel = hiltViewModel(),
) {
    val screenState by editPostViewModel.screenState.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localView = LocalView.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(screenState) {
        tagManagerViewModel.setSuggestedTags(screenState.suggestedTags)

        when {
            screenState.saved -> {
                localView.showBanner(R.string.posts_saved_feedback)
                editPostViewModel.userNotified()
            }

            screenState.invalidUrlError.isNotEmpty() || screenState.invalidTitleError.isNotEmpty() -> {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        floatingActionButton = MainState.FabComponent.Visible(
                            id = actionId,
                            icon = R.drawable.ic_done,
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

    LaunchedEffect(Unit) {
        editPostViewModel.postState
            .onEach { post ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(localContext.getString(R.string.posts_add_title)),
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(icon = R.drawable.ic_close),
                        bottomAppBar = MainState.BottomAppBarComponent.Visible(
                            id = actionId,
                            menuItems = buildList {
                                if (post.id.isNotEmpty()) {
                                    add(MainState.MenuItemComponent.DeleteBookmark)
                                    add(MainState.MenuItemComponent.OpenInBrowser)
                                }
                            },
                            navigationIcon = null,
                            data = post,
                        ),
                        floatingActionButton = MainState.FabComponent.Visible(actionId, R.drawable.ic_done),
                    )
                }
            }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}

@Composable
private fun LaunchedPostDetailViewModelEffect(
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
) {
    val screenState by postDetailViewModel.screenState.collectAsStateWithLifecycle()

    val localView = LocalView.current

    LaunchedEffect(screenState) {
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
    isLoading: Boolean,
    onUrlChanged: (String) -> Unit,
    urlError: String,
    onTitleChanged: (String) -> Unit,
    titleError: String,
    onDescriptionChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onPrivateChanged: (Boolean) -> Unit,
    onReadLaterChanged: (Boolean) -> Unit,
    searchTagInput: String,
    onSearchTagInputChanged: (String) -> Unit,
    onAddTagClicked: (String) -> Unit,
    suggestedTags: List<String>,
    onSuggestedTagClicked: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: List<Tag>,
    onRemoveCurrentTagClicked: (Tag) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExtendedTheme.colors.backgroundNoOverlay),
    ) {
        BookmarkContent(
            appMode = appMode,
            post = post,
            onUrlChanged = onUrlChanged,
            urlError = urlError,
            onTitleChanged = onTitleChanged,
            titleError = titleError,
            onDescriptionChanged = onDescriptionChanged,
            onNotesChanged = onNotesChanged,
            onPrivateChanged = onPrivateChanged,
            onReadLaterChanged = onReadLaterChanged,
            searchTagInput = searchTagInput,
            onSearchTagInputChanged = onSearchTagInputChanged,
            onAddTagClicked = onAddTagClicked,
            suggestedTags = suggestedTags,
            onSuggestedTagClicked = onSuggestedTagClicked,
            currentTagsTitle = currentTagsTitle,
            currentTags = currentTags,
            onRemoveCurrentTagClicked = onRemoveCurrentTagClicked,
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = ExtendedTheme.colors.backgroundNoOverlay),
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
    onUrlChanged: (String) -> Unit,
    urlError: String,
    onTitleChanged: (String) -> Unit,
    titleError: String,
    onDescriptionChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onPrivateChanged: (Boolean) -> Unit,
    onReadLaterChanged: (Boolean) -> Unit,
    searchTagInput: String,
    onSearchTagInputChanged: (String) -> Unit,
    onAddTagClicked: (String) -> Unit,
    suggestedTags: List<String>,
    onSuggestedTagClicked: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: List<Tag>,
    onRemoveCurrentTagClicked: (Tag) -> Unit,
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
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        BookmarkBasicDetails(
            appMode = appMode,
            url = post.url,
            onUrlChanged = onUrlChanged,
            urlError = urlError,
            title = post.displayTitle,
            onTitleChanged = onTitleChanged,
            titleError = titleError,
            description = post.displayDescription,
            onDescriptionChanged = onDescriptionChanged,
            notes = post.notes.orEmpty(),
            onNotesChanged = onNotesChanged,
        )

        BookmarkFlags(
            appMode = appMode,
            private = post.private,
            onPrivateChanged = onPrivateChanged,
            readLater = post.readLater,
            onReadLaterChanged = onReadLaterChanged,
        )

        TagManager(
            searchTagInput = searchTagInput,
            onSearchTagInputChanged = onSearchTagInputChanged,
            onAddTagClicked = onAddTagClicked,
            suggestedTags = suggestedTags,
            onSuggestedTagClicked = onSuggestedTagClicked,
            currentTagsTitle = currentTagsTitle,
            currentTags = currentTags,
            onRemoveCurrentTagClicked = onRemoveCurrentTagClicked,
            modifier = Modifier.padding(bottom = 100.dp),
        )
    }
}

@Composable
private fun BookmarkBasicDetails(
    appMode: AppMode,
    url: String,
    onUrlChanged: (String) -> Unit,
    urlError: String,
    title: String,
    onTitleChanged: (String) -> Unit,
    titleError: String,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    notes: String,
    onNotesChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        val focusManager = LocalFocusManager.current
        val (frUrl, frTitle, frDescription, frNotes) = FocusRequester.createRefs()
        var focusedField by rememberSaveable { mutableStateOf(FocusedField.NONE) }

        LaunchedEffect(Unit) {
            when (focusedField) {
                FocusedField.NONE -> Unit
                FocusedField.URL -> frUrl.requestFocus()
                FocusedField.TITLE -> frTitle.requestFocus()
                FocusedField.DESCRIPTION -> frDescription.requestFocus()
                FocusedField.NOTES -> frNotes.requestFocus()
            }
        }

        var urlField by remember {
            mutableStateOf(TextFieldValue(text = url, selection = TextRange(url.length)))
        }
        OutlinedTextField(
            value = urlField,
            onValueChange = { newValue ->
                urlField = newValue
                onUrlChanged(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(frUrl)
                .onFocusChanged { if (it.hasFocus) focusedField = FocusedField.URL },
            label = { Text(text = stringResource(id = R.string.posts_add_url)) },
            supportingText = {
                if (urlError.isNotEmpty()) {
                    Text(text = urlError)
                }
            },
            isError = urlError.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Next) },
            singleLine = true,
        )

        var titleField by remember {
            mutableStateOf(TextFieldValue(text = title, selection = TextRange(title.length)))
        }
        OutlinedTextField(
            value = titleField,
            onValueChange = { newValue ->
                val coerced = newValue.copy(text = newValue.text.take(AppConfig.PinboardApiMaxLength.TEXT_TYPE.value))
                titleField = coerced
                onTitleChanged(coerced.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(frTitle)
                .onFocusChanged { if (it.hasFocus) focusedField = FocusedField.TITLE },
            label = { Text(text = stringResource(id = R.string.posts_add_url_title)) },
            supportingText = {
                if (titleError.isNotEmpty()) {
                    Text(text = titleError)
                }
            },
            isError = titleError.isNotEmpty(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions { focusManager.moveFocus(FocusDirection.Next) },
        )

        var descriptionField by remember {
            mutableStateOf(TextFieldValue(text = description, selection = TextRange(description.length)))
        }
        OutlinedTextField(
            value = descriptionField,
            onValueChange = { newValue ->
                descriptionField = newValue
                onDescriptionChanged(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(frDescription)
                .onFocusChanged { if (it.hasFocus) focusedField = FocusedField.DESCRIPTION },
            label = { Text(text = stringResource(id = R.string.posts_add_url_description)) },
            supportingText = {},
        )

        if (AppMode.LINKDING == appMode) {
            var notesField by remember {
                mutableStateOf(TextFieldValue(text = notes, selection = TextRange(notes.length)))
            }
            OutlinedTextField(
                value = notesField,
                onValueChange = { newValue ->
                    notesField = newValue
                    onNotesChanged(newValue.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(frNotes)
                    .onFocusChanged { if (it.hasFocus) focusedField = FocusedField.NOTES },
                label = { Text(text = stringResource(id = R.string.posts_add_url_notes)) },
            )
        }
    }
}

private enum class FocusedField {
    NONE,
    URL,
    TITLE,
    DESCRIPTION,
    NOTES,
}

@Composable
private fun BookmarkFlags(
    appMode: AppMode,
    private: Boolean?,
    onPrivateChanged: (Boolean) -> Unit,
    readLater: Boolean?,
    onReadLaterChanged: (Boolean) -> Unit,
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
                onCheckedChange = onPrivateChanged,
                modifier = Modifier.weight(0.5f),
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.posts_add_read_later),
            description = null,
            checked = readLater == true,
            onCheckedChange = onReadLaterChanged,
            modifier = Modifier.weight(0.5f),
        )
    }
}
// endregion Content

// region Previews
@Composable
@ThemePreviews
private fun EditBookmarkScreenPreview(
    @PreviewParameter(provider = PostProvider::class) post: Post,
) {
    ExtendedTheme {
        EditBookmarkScreen(
            appMode = AppMode.PINBOARD,
            post = post.copy(description = post.description.take(200)),
            isLoading = false,
            onUrlChanged = {},
            urlError = "",
            onTitleChanged = {},
            titleError = "",
            onDescriptionChanged = {},
            onNotesChanged = {},
            onPrivateChanged = {},
            onReadLaterChanged = {},
            searchTagInput = "",
            onSearchTagInputChanged = {},
            onAddTagClicked = {},
            suggestedTags = emptyList(),
            onSuggestedTagClicked = {},
            currentTagsTitle = stringResource(id = R.string.tags_added_title),
            currentTags = post.tags.orEmpty(),
            onRemoveCurrentTagClicked = {},
        )
    }
}
// endregion Previews
