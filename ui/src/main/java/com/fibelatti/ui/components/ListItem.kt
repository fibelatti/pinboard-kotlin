package com.fibelatti.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fibelatti.ui.foundation.Shapes

public object ListItem {

    public val MinHeight: Dp = 64.dp

    public val DefaultShape: Shape = Shapes.StandaloneShape
}

@Composable
public fun ListItem(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = defaultListItemColors(),
    shape: Shape = ListItem.DefaultShape,
    headlineFlag: @Composable RowScope.() -> Unit = {},
) {
    androidx.compose.material3.ListItem(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListItem.MinHeight),
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = { ListItemSupportingText(text = supportingText) },
        verticalAlignment = Alignment.CenterVertically,
        shapes = ListItemDefaults.shapes(shape = shape),
        colors = colors,
        content = { ListItemHeadline(text = headlineText, flag = headlineFlag) },
    )
}

@Composable
public fun ListItem(
    headlineText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    supportingText: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = defaultListItemColors(),
    shape: Shape = ListItem.DefaultShape,
    headlineFlag: @Composable RowScope.() -> Unit = {},
) {
    androidx.compose.material3.ListItem(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListItem.MinHeight),
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = { ListItemSupportingText(text = supportingText) },
        verticalAlignment = Alignment.CenterVertically,
        onLongClick = onLongClick,
        shapes = ListItemDefaults.shapes(shape = shape),
        colors = colors,
        content = { ListItemHeadline(text = headlineText, flag = headlineFlag) },
    )
}

@Composable
public fun defaultListItemColors(): ListItemColors = ListItemDefaults.colors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
public fun selectableListItemColors(): ListItemColors = defaultListItemColors().copy(
    supportingContentColor = MaterialTheme.colorScheme.onSurface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedSupportingContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

@Composable
private fun ListItemHeadline(
    text: String,
    flag: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.titleMedium,
        )

        flag()
    }
}

@Composable
private fun ListItemSupportingText(text: String?) {
    if (!text.isNullOrEmpty()) {
        Text(
            text = text,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
