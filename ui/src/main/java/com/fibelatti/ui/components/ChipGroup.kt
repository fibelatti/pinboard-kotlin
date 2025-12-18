package com.fibelatti.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fibelatti.ui.R
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
public fun Chip(
    item: ChipGroup.Item,
    onClick: (ChipGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    onIconClick: (ChipGroup.Item) -> Unit = onClick,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    tonalElevation: Dp = ChipGroup.TonalElevation,
    colors: ChipGroup.Colors = ChipGroup.colors(),
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = ChipGroup.MinSize),
        shape = shape,
        color = if (item.isSelected) colors.selectedChipColor else colors.unselectedChipColor,
        tonalElevation = tonalElevation,
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick(item) }
                .padding(
                    start = 12.dp,
                    end = if (item.icon == EmptyPainter) 12.dp else 0.dp,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.text,
                maxLines = 1,
                style = textStyle,
            )
            if (item.icon != EmptyPainter) {
                val interactionSource = remember { MutableInteractionSource() }
                Icon(
                    painter = item.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable(interactionSource = interactionSource, indication = null) { onIconClick(item) }
                        .padding(all = 8.dp)
                        .size(20.dp),
                )
            }
        }
    }
}

@Composable
public fun MultilineChipGroup(
    items: List<ChipGroup.Item>,
    onItemClick: (ChipGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    onItemIconClick: (ChipGroup.Item) -> Unit = onItemClick,
    itemShape: CornerBasedShape = MaterialTheme.shapes.small,
    itemTonalElevation: Dp = ChipGroup.TonalElevation,
    itemColors: ChipGroup.Colors = ChipGroup.colors(),
    itemTextStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    spacing: Dp = ChipGroup.Spacing,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        items.forEach { item ->
            Chip(
                item = item,
                onClick = onItemClick,
                onIconClick = onItemIconClick,
                shape = itemShape,
                tonalElevation = itemTonalElevation,
                colors = itemColors,
                textStyle = itemTextStyle,
            )
        }
    }
}

@Composable
public fun SingleLineChipGroup(
    items: List<ChipGroup.Item>,
    onItemClick: (ChipGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    onItemIconClick: (ChipGroup.Item) -> Unit = onItemClick,
    itemShape: CornerBasedShape = MaterialTheme.shapes.small,
    itemTonalElevation: Dp = ChipGroup.TonalElevation,
    itemColors: ChipGroup.Colors = ChipGroup.colors(),
    itemTextStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    spacing: Dp = ChipGroup.Spacing,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    header: @Composable LazyItemScope.() -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        stickyHeader(key = "header") {
            header()
        }

        items(items, key = { it.hashCode() }) { item ->
            Chip(
                item = item,
                onClick = onItemClick,
                modifier = Modifier.animateItem(),
                onIconClick = onItemIconClick,
                shape = itemShape,
                tonalElevation = itemTonalElevation,
                colors = itemColors,
                textStyle = itemTextStyle,
            )
        }
    }
}

public object ChipGroup {

    public val MinSize: Dp = 40.dp
    internal val Spacing: Dp = 4.dp
    internal val TonalElevation: Dp = 2.dp

    @Immutable
    public data class Item(
        val text: String,
        val icon: Painter = EmptyPainter,
        val isSelected: Boolean = false,
    )

    @Immutable
    public data class Colors(
        val selectedChipColor: Color,
        val unselectedChipColor: Color,
    )

    @Composable
    public fun colors(
        selectedChipColor: Color = MaterialTheme.colorScheme.primary,
        unselectedChipColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ): Colors = Colors(
        selectedChipColor = selectedChipColor,
        unselectedChipColor = unselectedChipColor,
    )
}

@Composable
@ThemePreviews
private fun MultilineChipGroupPreview() {
    ExtendedTheme {
        val tags = listOf("kotlin", "dev", "ui", "android", "compose", "dependency-injection", "testing")
        val items = tags.mapIndexed { index, tag ->
            ChipGroup.Item(
                text = tag,
                icon = painterResource(id = R.drawable.ic_close),
                isSelected = index == 0,
            )
        }

        MultilineChipGroup(
            items = items,
            onItemClick = {},
            modifier = Modifier.padding(8.dp),
            spacing = 8.dp,
        )
    }
}

@Composable
@ThemePreviews
private fun SingleLineChipGroupPreview() {
    ExtendedTheme {
        val tags = listOf("kotlin", "dev", "ui", "android", "compose", "dependency-injection", "testing")
        val items = tags.mapIndexed { index, tag ->
            ChipGroup.Item(
                text = tag,
                icon = painterResource(id = R.drawable.ic_close),
                isSelected = index == 0,
            )
        }

        SingleLineChipGroup(
            items = items,
            onItemClick = {},
            modifier = Modifier.padding(8.dp),
            spacing = 8.dp,
        )
    }
}
