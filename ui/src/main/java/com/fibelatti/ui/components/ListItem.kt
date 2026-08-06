package com.fibelatti.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    shape: Shape = ListItem.DefaultShape,
    headlineFlag: @Composable RowScope.() -> Unit = {},
) {
    androidx.compose.material3.ListItem(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListItem.MinHeight)
            .clip(shape),
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = {
            if (!supportingText.isNullOrEmpty()) {
                AutoSizeText(
                    text = supportingText,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 4,
                    minFontSize = 8.sp,
                )
            }
        },
        colors = colors,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AutoSizeText(
                    text = headlineText,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                )

                headlineFlag()
            }
        },
    )
}
