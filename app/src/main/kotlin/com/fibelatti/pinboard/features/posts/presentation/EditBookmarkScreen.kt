package com.fibelatti.pinboard.features.posts.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.SettingToggle
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagManager
import com.fibelatti.pinboard.features.tags.presentation.TagManagerViewModel
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.rememberKeyboardState
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun EditBookmarkScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    editPostViewModel: EditPostViewModel = hiltViewModel(),
    postDetailViewModel: PostDetailViewModel = hiltViewModel(),
    tagManagerViewModel: TagManagerViewModel = hiltViewModel(),
    mainVariant: Boolean,
) {
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
                tags = it.defaultTags,
            )

            editPostViewModel.initializePost(emptyPost)
            tagManagerViewModel.setTags(it.defaultTags)
        }
    }

    LaunchedEffect(editPostContent) {
        editPostContent?.let {
            editPostViewModel.initializePost(it.post)
            tagManagerViewModel.setTags(it.post.tags.orEmpty())
        }
    }

    val postState by editPostViewModel.postState.collectAsStateWithLifecycle(initialValue = null)
    val currentState = postState ?: return

    val isEditLoading by editPostViewModel.loading.collectAsStateWithLifecycle(initialValue = false)
    val isDetailLoading by postDetailViewModel.loading.collectAsStateWithLifecycle(initialValue = false)

    val urlError by editPostViewModel.invalidUrlError.collectAsStateWithLifecycle()
    val titleError by editPostViewModel.invalidUrlTitleError.collectAsStateWithLifecycle()

    val tagManagerState by tagManagerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(tagManagerState) {
        editPostViewModel.searchForTag(tagManagerState.currentQuery, tagManagerState.tags)
        editPostViewModel.updatePost { post -> post.copy(tags = tagManagerState.tags) }
    }

    EditBookmarkScreen(
        post = currentState,
        isLoading = isEditLoading || isDetailLoading,
        onUrlChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(url = newValue) }
        },
        urlError = urlError,
        onTitleChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(title = newValue) }
        },
        titleError = titleError,
        onDescriptionChanged = { newValue ->
            editPostViewModel.updatePost { post -> post.copy(description = newValue) }
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
        suggestedTags = tagManagerState.suggestedTags.toStableList(),
        onSuggestedTagClicked = tagManagerViewModel::addTag,
        currentTagsTitle = stringResource(id = tagManagerState.displayTitle),
        currentTags = tagManagerState.tags.toStableList(),
        onRemoveCurrentTagClicked = tagManagerViewModel::removeTag,
        mainVariant = mainVariant,
    )
}

@Composable
private fun EditBookmarkScreen(
    post: Post,
    isLoading: Boolean,
    onUrlChanged: (String) -> Unit,
    urlError: String,
    onTitleChanged: (String) -> Unit,
    titleError: String,
    onDescriptionChanged: (String) -> Unit,
    onPrivateChanged: (Boolean) -> Unit,
    onReadLaterChanged: (Boolean) -> Unit,
    searchTagInput: String,
    onSearchTagInputChanged: (String) -> Unit,
    onAddTagClicked: (String) -> Unit,
    suggestedTags: StableList<String>,
    onSuggestedTagClicked: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: StableList<Tag>,
    onRemoveCurrentTagClicked: (Tag) -> Unit,
    mainVariant: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExtendedTheme.colors.backgroundNoOverlay),
    ) {
        BookmarkContent(
            post = post,
            onUrlChanged = onUrlChanged,
            urlError = urlError,
            onTitleChanged = onTitleChanged,
            titleError = titleError,
            onDescriptionChanged = onDescriptionChanged,
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
            mainVariant = mainVariant,
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
    post: Post,
    onUrlChanged: (String) -> Unit,
    urlError: String,
    onTitleChanged: (String) -> Unit,
    titleError: String,
    onDescriptionChanged: (String) -> Unit,
    onPrivateChanged: (Boolean) -> Unit,
    onReadLaterChanged: (Boolean) -> Unit,
    searchTagInput: String,
    onSearchTagInputChanged: (String) -> Unit,
    onAddTagClicked: (String) -> Unit,
    suggestedTags: StableList<String>,
    onSuggestedTagClicked: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: StableList<Tag>,
    onRemoveCurrentTagClicked: (Tag) -> Unit,
    mainVariant: Boolean,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .imePadding(),
    ) {
        if (post.pendingSync != null) {
            PendingSyncIndicator(
                text = when (post.pendingSync) {
                    PendingSync.ADD -> stringResource(id = R.string.posts_pending_add_expanded)
                    PendingSync.UPDATE -> stringResource(id = R.string.posts_pending_update_expanded)
                    PendingSync.DELETE -> stringResource(id = R.string.posts_pending_delete_expanded)
                },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        BookmarkBasicDetails(
            url = post.url,
            onUrlChanged = onUrlChanged,
            urlError = urlError,
            title = post.title,
            onTitleChanged = onTitleChanged,
            titleError = titleError,
            description = post.description,
            onDescriptionChanged = onDescriptionChanged,
        )

        BookmarkFlags(
            mainVariant = mainVariant,
            private = post.private,
            onPrivateChanged = onPrivateChanged,
            readLater = post.readLater,
            onReadLaterChanged = onReadLaterChanged,
        )

        val imeVisible by rememberKeyboardState()
        var tagInputHasFocus by remember { mutableStateOf(false) }
        var tagInputTop by remember { mutableStateOf(0f) }

        TagManager(
            searchTagInput = searchTagInput,
            onSearchTagInputChanged = onSearchTagInputChanged,
            onAddTagClicked = onAddTagClicked,
            suggestedTags = suggestedTags,
            onSuggestedTagClicked = onSuggestedTagClicked,
            currentTagsTitle = currentTagsTitle,
            currentTags = currentTags,
            onRemoveCurrentTagClicked = onRemoveCurrentTagClicked,
            onSearchTagInputFocusChanged = { hasFocus -> tagInputHasFocus = hasFocus },
            modifier = Modifier
                .padding(bottom = 100.dp)
                .onGloballyPositioned { tagInputTop = it.boundsInParent().top },
        )

        LaunchedEffect(imeVisible, tagInputHasFocus, tagInputTop) {
            if (imeVisible && tagInputHasFocus && scrollState.canScrollForward) {
                scrollState.animateScrollTo(
                    value = tagInputTop.toInt(),
                    animationSpec = tween(durationMillis = 200, delayMillis = 300, easing = LinearEasing),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun BookmarkBasicDetails(
    url: String,
    onUrlChanged: (String) -> Unit,
    urlError: String,
    title: String,
    onTitleChanged: (String) -> Unit,
    titleError: String,
    description: String,
    onDescriptionChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        val focusManager = LocalFocusManager.current
        val (frUrl, frTitle, frDescription) = FocusRequester.createRefs()
        var focusedField by rememberSaveable { mutableStateOf(FocusedField.NONE) }
        val imeController = LocalSoftwareKeyboardController.current

        LaunchedEffect(Unit) {
            when (focusedField) {
                FocusedField.NONE -> Unit
                FocusedField.URL -> frUrl.requestFocus()
                FocusedField.TITLE -> frTitle.requestFocus()
                FocusedField.DESCRIPTION -> frDescription.requestFocus()
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
        val titleMaxLength = integerResource(id = R.integer.api_max_title_length)
        OutlinedTextField(
            value = titleField,
            onValueChange = { newValue ->
                if (newValue.text.length <= titleMaxLength) {
                    titleField = newValue
                    onTitleChanged(newValue.text)
                }
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
                imeController?.hide()
            },
        )
    }
}

private enum class FocusedField {
    NONE, URL, TITLE, DESCRIPTION,
}

@Composable
private fun BookmarkFlags(
    mainVariant: Boolean,
    private: Boolean,
    onPrivateChanged: (Boolean) -> Unit,
    readLater: Boolean,
    onReadLaterChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (mainVariant) {
            SettingToggle(
                title = stringResource(id = R.string.posts_add_private),
                description = null,
                checked = private,
                onCheckedChange = onPrivateChanged,
                modifier = Modifier.weight(0.5f),
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.posts_add_read_later),
            description = null,
            checked = readLater,
            onCheckedChange = onReadLaterChanged,
            modifier = Modifier.weight(0.5f),
        )
    }
}

@Composable
@ThemePreviews
private fun EditBookmarkScreenPreview(
    @PreviewParameter(provider = PostProvider::class) post: Post,
) {
    ExtendedTheme {
        EditBookmarkScreen(
            post = post.copy(description = post.description.take(200)),
            isLoading = false,
            onUrlChanged = {},
            urlError = "",
            onTitleChanged = {},
            titleError = "",
            onDescriptionChanged = {},
            onPrivateChanged = {},
            onReadLaterChanged = {},
            searchTagInput = "",
            onSearchTagInputChanged = {},
            onAddTagClicked = {},
            suggestedTags = StableList(),
            onSuggestedTagClicked = {},
            currentTagsTitle = stringResource(id = R.string.tags_added_title),
            currentTags = post.tags.orEmpty().toStableList(),
            onRemoveCurrentTagClicked = {},
            mainVariant = true,
        )
    }
}
