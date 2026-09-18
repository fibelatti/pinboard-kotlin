package com.fibelatti.pinboard.features.tags.presentation

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.fillWidthOfParent
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.foundation.Shapes
import com.fibelatti.ui.icons.Close
import com.fibelatti.ui.icons.UiIcons
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme

/**
 * Title of the section listing the tags currently added, which reflects whether there are any.
 */
val TagManagerState.displayTitle: String
    @Composable
    @ReadOnlyComposable
    get() = stringResource(id = if (tags.isEmpty()) R.string.tags_empty_title else R.string.tags_added_title)

@Composable
fun TagManager(
    searchTagInput: String,
    onSearchTagInputChange: (String) -> Unit,
    onAddTagClick: (String) -> Unit,
    suggestedTags: List<String>,
    onSuggestedTagClick: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: List<Tag>,
    onRemoveCurrentTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    onSearchTagInputFocusChange: (hasFocus: Boolean) -> Unit = {},
    horizontalPadding: Dp = 16.dp,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val keyboardAction = {
            when (val text = searchTagInput.trim()) {
                "" -> keyboardController?.hide()
                else -> onAddTagClick(text)
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = searchTagInput,
                onValueChange = { newValue ->
                    when {
                        // Handle keyboards that add a space after punctuation, `.` is used for private tags
                        newValue == ". " -> onSearchTagInputChange(".")

                        newValue.isNotBlank() && newValue.endsWith(" ") -> onAddTagClick(newValue)

                        else -> onSearchTagInputChange(newValue)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            keyboardAction()
                            return@onKeyEvent true
                        }
                        false
                    }
                    .onFocusChanged { onSearchTagInputFocusChange(it.hasFocus) },
                textStyle = MaterialTheme.typography.bodyMedium,
                label = { Text(text = stringResource(id = R.string.posts_add_tags)) },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            if (searchTagInput.isNotBlank()) {
                                onAddTagClick(searchTagInput)
                            }
                        },
                        shapes = ExtendedTheme.defaultButtonShapes,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text(text = stringResource(id = R.string.hint_add))
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions { keyboardAction() },
                singleLine = true,
                maxLines = 1,
                shape = Shapes.StandaloneShape,
            )
        }

        if (suggestedTags.isNotEmpty()) {
            SingleLineChipGroup(
                items = remember(suggestedTags) {
                    suggestedTags.map { tag -> ChipGroup.Item(text = tag) }
                },
                onItemClick = { item -> onSuggestedTagClick(suggestedTags.first { it == item.text }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillWidthOfParent(parentPaddingStart = horizontalPadding, parentPaddingEnd = horizontalPadding),
                itemTextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = currentTagsTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        val closeIcon = rememberVectorPainter(UiIcons.Close)

        MultilineChipGroup(
            items = remember(currentTags) {
                currentTags.map { tag -> ChipGroup.Item(text = tag.name, icon = closeIcon) }
            },
            onItemClick = {},
            onItemIconClick = { item -> onRemoveCurrentTagClick(currentTags.first { it.name == item.text }) },
            itemTextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
@PreviewAll
private fun TagManagerPreview() {
    ExtendedTheme {
        TagManager(
            searchTagInput = "",
            onSearchTagInputChange = {},
            onAddTagClick = {},
            suggestedTags = listOf("Android", "Dev"),
            onSuggestedTagClick = {},
            currentTagsTitle = stringResource(id = R.string.tags_added_title),
            currentTags = listOf(Tag(name = "Kotlin"), Tag(name = "Compose")),
            onRemoveCurrentTagClick = {},
        )
    }
}
