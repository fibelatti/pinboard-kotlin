package com.fibelatti.pinboard.features.tags.presentation

import android.view.KeyEvent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.MultilineChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun TagManager(
    searchTagInput: String,
    onSearchTagInputChanged: (String) -> Unit,
    onAddTagClicked: (String) -> Unit,
    suggestedTags: List<String>,
    onSuggestedTagClicked: (String) -> Unit,
    currentTagsTitle: String,
    currentTags: List<Tag>,
    onRemoveCurrentTagClicked: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    onSearchTagInputFocusChanged: (hasFocus: Boolean) -> Unit = {},
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 32.dp),
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val keyboardAction = {
            when (val text = searchTagInput.trim()) {
                "" -> keyboardController?.hide()
                else -> onAddTagClicked(text)
            }
        }

        val (
            clAddTagInput, clAddTagButton,
            clSuggestedTags,
            clDivider,
            clCurrentTagsTitle,
            clCurrentTags,
        ) = createRefs()

        val horizontalPadding = 16.dp

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
                    start.linkTo(parent.start, margin = horizontalPadding)
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
                }
                .onFocusChanged { onSearchTagInputFocusChanged(it.hasFocus) },
            textStyle = MaterialTheme.typography.bodyMedium,
            label = { Text(text = stringResource(id = R.string.posts_add_tags)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions { keyboardAction() },
            singleLine = true,
            maxLines = 1,
        )

        FilledTonalButton(
            onClick = {
                if (searchTagInput.isNotBlank()) {
                    onAddTagClicked(searchTagInput)
                }
            },
            modifier = Modifier.constrainAs(clAddTagButton) {
                bottom.linkTo(clAddTagInput.bottom, margin = 4.dp)
                start.linkTo(clAddTagInput.end, margin = 8.dp)
                end.linkTo(parent.end, margin = horizontalPadding)
            },
        ) {
            Text(
                text = stringResource(id = R.string.posts_add_tags_add),
                fontSize = 14.sp,
            )
        }

        if (suggestedTags.isNotEmpty()) {
            SingleLineChipGroup(
                items = remember(suggestedTags) {
                    suggestedTags.map { tag -> ChipGroup.Item(text = tag) }
                },
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
                ),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
            )

            HorizontalDivider(
                modifier = Modifier.constrainAs(clDivider) {
                    start.linkTo(parent.start, margin = horizontalPadding)
                    top.linkTo(clSuggestedTags.bottom, margin = 8.dp)
                    end.linkTo(parent.end, margin = horizontalPadding)
                    width = Dimension.fillToConstraints
                },
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = currentTagsTitle,
            modifier = Modifier
                .constrainAs(clCurrentTagsTitle) {
                    start.linkTo(parent.start, margin = horizontalPadding)
                    top.linkTo(
                        anchor = if (suggestedTags.isNotEmpty()) clDivider.bottom else clAddTagInput.bottom,
                        margin = 16.dp,
                    )
                    end.linkTo(parent.end, margin = horizontalPadding)
                    width = Dimension.fillToConstraints
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        val closeIcon = painterResource(id = R.drawable.ic_close)

        MultilineChipGroup(
            items = remember(currentTags) {
                currentTags.map { tag -> ChipGroup.Item(text = tag.name, icon = closeIcon) }
            },
            onItemClick = {},
            modifier = Modifier.constrainAs(clCurrentTags) {
                start.linkTo(parent.start, margin = horizontalPadding)
                top.linkTo(clCurrentTagsTitle.bottom, margin = 8.dp)
                end.linkTo(parent.end, margin = horizontalPadding)
                width = Dimension.fillToConstraints
            },
            itemTonalElevation = 16.dp,
            onItemIconClick = { item -> onRemoveCurrentTagClicked(currentTags.first { it.name == item.text }) },
            itemColors = ChipGroup.colors(
                unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            itemTextStyle = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.SansSerif,
            ),
        )
    }
}

@Composable
@ThemePreviews
private fun TagManagerPreview() {
    ExtendedTheme {
        TagManager(
            searchTagInput = "",
            onSearchTagInputChanged = {},
            onAddTagClicked = {},
            suggestedTags = listOf("Android", "Dev"),
            onSuggestedTagClicked = {},
            currentTagsTitle = stringResource(id = R.string.tags_added_title),
            currentTags = listOf(Tag(name = "Kotlin"), Tag(name = "Compose")),
            onRemoveCurrentTagClicked = {},
        )
    }
}
