@file:Suppress("LongMethod")

package com.fibelatti.pinboard.features.tags.presentation

import android.view.KeyEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun TagManager(
    tagManagerViewModel: TagManagerViewModel = hiltViewModel(),
) {
    val state by tagManagerViewModel.state.collectAsStateWithLifecycle()

    TagManager(
        searchTagInput = state.currentQuery,
        onSearchTagInputChanged = tagManagerViewModel::setQuery,
        onAddTagClicked = tagManagerViewModel::addTag,
        suggestedTags = state.suggestedTags,
        onSuggestedTagClicked = tagManagerViewModel::addTag,
        currentTagsTitle = stringResource(id = state.displayTitle),
        currentTags = state.tags,
        onRemoveCurrentTagClicked = tagManagerViewModel::removeTag,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun TagManager(
    searchTagInput: String = "",
    onSearchTagInputChanged: (String) -> Unit = {},
    onAddTagClicked: (String) -> Unit = {},
    suggestedTags: List<String> = emptyList(),
    onSuggestedTagClicked: (String) -> Unit = {},
    currentTagsTitle: String = stringResource(id = R.string.tags_empty_title),
    currentTags: List<Tag> = emptyList(),
    onRemoveCurrentTagClicked: (Tag) -> Unit = {},
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 32.dp),
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val keyboardAction = {
            when (val text = searchTagInput.trim()) {
                "" -> keyboardController?.hide()
                else -> {
                    onAddTagClicked(text)
                }
            }
        }

        val (
            clAddTagInput, clAddTagButton,
            clSuggestedTags,
            clDivider,
            clCurrentTagsTitle,
            clCurrentTags,
        ) = createRefs()

        OutlinedTextField(
            value = searchTagInput,
            onValueChange = { newValue ->
                when {
                    // Handle keyboards that add a space after punctuation, . is used for private tags
                    newValue == ". " -> onSearchTagInputChanged(".")
                    newValue.isNotBlank() && newValue.endsWith(" ") -> onAddTagClicked(newValue)
                    else -> onSearchTagInputChanged(newValue)
                }
            },
            modifier = Modifier
                .constrainAs(clAddTagInput) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(clAddTagButton.start)
                    width = Dimension.fillToConstraints
                }
                .onKeyEvent {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        keyboardAction()
                        return@onKeyEvent true
                    }
                    false
                },
            textStyle = MaterialTheme.typography.bodyMedium,
            label = { Text(text = stringResource(id = R.string.posts_add_tags)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions { keyboardAction() },
            singleLine = true,
            maxLines = 1,
        )

        ElevatedButton(
            onClick = {
                if (searchTagInput.isNotBlank()) {
                    onAddTagClicked(searchTagInput)
                }
            },
            modifier = Modifier.constrainAs(clAddTagButton) {
                start.linkTo(clAddTagInput.end, margin = 8.dp)
                top.linkTo(clAddTagInput.top)
                end.linkTo(parent.end)
                bottom.linkTo(clAddTagInput.bottom)
            },
        ) {
            Text(
                text = stringResource(id = R.string.posts_add_tags_add),
                fontSize = 14.sp,
            )
        }

        if (suggestedTags.isNotEmpty()) {
            SingleLineChipGroup(
                items = suggestedTags.map { tag -> ChipGroup.Item(text = tag) },
                onItemClick = { item -> onSuggestedTagClicked(suggestedTags.first { it == item.text }) },
                modifier = Modifier
                    .constrainAs(clSuggestedTags) {
                        start.linkTo(parent.start)
                        top.linkTo(clAddTagInput.bottom, margin = 16.dp)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(),
                itemColors = ChipGroup.colors(
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                itemTextStyle = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                )
            )

            Divider(
                modifier = Modifier.constrainAs(clDivider) {
                    start.linkTo(parent.start, margin = 4.dp)
                    top.linkTo(clSuggestedTags.bottom, margin = 8.dp)
                    end.linkTo(parent.end, margin = 4.dp)
                },
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = currentTagsTitle,
            modifier = Modifier
                .constrainAs(clCurrentTagsTitle) {
                    start.linkTo(parent.start)
                    top.linkTo(
                        anchor = if (suggestedTags.isNotEmpty()) clDivider.bottom else clAddTagInput.bottom,
                        margin = 16.dp
                    )
                    end.linkTo(parent.end)
                }
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        MultilineChipGroup(
            items = currentTags.map { tag ->
                ChipGroup.Item(
                    text = tag.name,
                    icon = painterResource(id = R.drawable.ic_close),
                )
            },
            onItemClick = {},
            modifier = Modifier.constrainAs(clCurrentTags) {
                start.linkTo(parent.start)
                top.linkTo(clCurrentTagsTitle.bottom, margin = 8.dp)
                end.linkTo(parent.end)
            },
            onItemIconClick = { item -> onRemoveCurrentTagClicked(currentTags.first { it.name == item.text }) },
            itemColors = ChipGroup.colors(
                unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            itemTextStyle = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.SansSerif,
            )
        )
    }
}

@Composable
@ThemePreviews
private fun TagManagerPreview() {
    ExtendedTheme {
        TagManager(
            suggestedTags = listOf("Android", "Dev"),
            currentTagsTitle = stringResource(id = R.string.tags_added_title),
            currentTags = listOf(Tag(name = "Kotlin"), Tag(name = "Compose"))
        )
    }
}

@Composable
@ThemePreviews
private fun EmptyTagManagerPreview() {
    ExtendedTheme {
        TagManager()
    }
}
