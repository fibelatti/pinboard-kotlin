package com.fibelatti.pinboard.core.android

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

object SelectionDialog {

    fun <T> show(
        context: Context,
        title: String,
        options: List<T>,
        optionName: (T) -> String,
        optionIcon: (T) -> Int? = { null },
        onOptionSelected: (T) -> Unit,
    ) {
        show(
            context = context,
            title = title,
            options = options.associateWith { false },
            optionName = optionName,
            optionIcon = optionIcon,
            onOptionSelected = onOptionSelected,
        )
    }

    fun <T> show(
        context: Context,
        title: String,
        options: Map<T, Boolean>,
        optionName: (T) -> String,
        optionIcon: (T) -> Int? = { null },
        onOptionSelected: (T) -> Unit,
    ) {
        ComposeBottomSheetDialog(context) {
            SelectionDialogContent(
                title = title,
                options = options,
                optionName = optionName,
                optionIcon = optionIcon,
                onOptionSelected = { option ->
                    onOptionSelected(option)
                    dismiss()
                },
            )
        }.show()
    }

    fun <T> showCustomizationDialog(
        context: Context,
        title: String,
        options: Map<T, Boolean>,
        optionName: (T) -> String,
        optionIcon: (T) -> Int? = { null },
        onConfirm: (Map<T, Boolean>) -> Unit,
    ) {
        ComposeBottomSheetDialog(context) {
            SelectionDialogCustomizationContent(
                title = title,
                options = options,
                optionName = optionName,
                optionIcon = optionIcon,
                onConfirm = { selectedOptions ->
                    onConfirm(selectedOptions)
                    dismiss()
                },
            )
        }.show()
    }
}

@Composable
private fun <T> SelectionDialogContent(
    title: String,
    options: Map<T, Boolean>,
    optionName: (T) -> String,
    optionIcon: (T) -> Int?,
    onOptionSelected: (T) -> Unit,
) {
    val visibleOptions = remember(options) { options.filterValues { hidden -> !hidden }.keys.toList() }
    val hiddenOptions = remember(options) { options.filterValues { hidden -> hidden }.keys.toList() }

    var showHiddenOptions by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .animateContentSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        items(visibleOptions) { option ->
            SelectionItem(
                option = option,
                optionName = optionName,
                optionIcon = optionIcon,
                onClick = onOptionSelected,
            )
        }

        when {
            hiddenOptions.isNotEmpty() && !showHiddenOptions -> {
                item {
                    TextButton(
                        onClick = { showHiddenOptions = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.quick_actions_more_options))
                    }
                }
            }

            hiddenOptions.isNotEmpty() -> {
                itemsIndexed(hiddenOptions) { index, option ->
                    SelectionItem(
                        option = option,
                        optionName = optionName,
                        optionIcon = optionIcon,
                        onClick = onOptionSelected,
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(delayMillis = 75 * index, easing = FastOutLinearInEasing),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> SelectionDialogCustomizationContent(
    title: String,
    options: Map<T, Boolean>,
    optionName: (T) -> String,
    optionIcon: (T) -> Int?,
    onConfirm: (Map<T, Boolean>) -> Unit,
) {
    var currentSelection by remember { mutableStateOf(options) }
    val visibleOptions by remember {
        derivedStateOf { currentSelection.filterValues { hidden -> !hidden }.keys.toList() }
    }
    val hiddenOptions by remember {
        derivedStateOf { currentSelection.filterValues { hidden -> hidden }.keys.toList() }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .animateContentSize(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = stringResource(R.string.quick_actions_customization_hidden_options_instruction),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        items(visibleOptions) { option ->
            SelectionItem(
                option = option,
                optionName = optionName,
                optionIcon = optionIcon,
                onClick = {
                    currentSelection = currentSelection.mapValues { item ->
                        if (item.key == option) true else item.value
                    }
                },
            )
        }

        if (hiddenOptions.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    Text(
                        text = stringResource(R.string.quick_actions_customization_hidden_options_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Text(
                        text = stringResource(R.string.quick_actions_customization_hidden_options_description),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            items(hiddenOptions) { option ->
                SelectionItem(
                    option = option,
                    optionName = optionName,
                    optionIcon = optionIcon,
                    onClick = {
                        currentSelection = currentSelection.mapValues { item ->
                            if (item.key == option) false else item.value
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(fraction = .8f)
                        .alpha(.6f),
                )
            }
        }

        item {
            TextButton(
                onClick = { onConfirm(currentSelection) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.quick_actions_customization_save_changes))
            }
        }
    }
}

@Composable
private fun <T> SelectionItem(
    option: T,
    optionName: (T) -> String,
    optionIcon: (T) -> Int?,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = { onClick(option) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = optionName(option),
            modifier = Modifier.weight(1F),
            textAlign = TextAlign.Center,
        )

        optionIcon(option)?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = "",
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// region Previews
@Composable
@ThemePreviews
private fun SelectionDialogContentPreview() {
    ExtendedTheme {
        SelectionDialogContent(
            title = "Select an option",
            options = listOf("Option 1", "Option 2", "Option 3").associateWith { false },
            optionName = { it },
            optionIcon = { null },
            onOptionSelected = {},
        )
    }
}

@Composable
@ThemePreviews
private fun SelectionDialogContentHiddenOptionsPreview() {
    ExtendedTheme {
        SelectionDialogContent(
            title = "Select an option",
            options = listOf("Option 1", "Option 2", "Option 3").associateWith { false }
                .plus(listOf("Option 4", "Option 5", "Option 6", "Option 7").associateWith { true }),
            optionName = { it },
            optionIcon = { null },
            onOptionSelected = {},
        )
    }
}
// endregion Previews
