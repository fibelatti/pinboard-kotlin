package com.fibelatti.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.fibelatti.ui.R
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun Chip(
    item: ChipGroup.Item,
    onClick: (ChipGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    onIconClick: (ChipGroup.Item) -> Unit = onClick,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    tonalElevation: Dp = ChipGroup.TonalElevation,
    colors: ChipGroup.Colors = ChipGroup.colors(),
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
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
                color = if (item.isSelected) colors.selectedTextColor else colors.unselectedTextColor,
                maxLines = 1,
                style = textStyle,
            )
            if (item.icon != EmptyPainter) {
                val interactionSource = remember { MutableInteractionSource() }
                Icon(
                    painter = item.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .defaultMinSize(minHeight = ChipGroup.MinSize)
                        .clickable(interactionSource = interactionSource, indication = null) { onIconClick(item) }
                        .padding(all = 8.dp),
                    tint = if (item.isSelected) colors.selectedIconColor else colors.unselectedIconColor,
                )
            }
        }
    }
}

@Composable
fun MultilineChipGroup(
    items: StableList<ChipGroup.Item>,
    onItemClick: (ChipGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    onItemIconClick: (ChipGroup.Item) -> Unit = onItemClick,
    itemShape: CornerBasedShape = MaterialTheme.shapes.small,
    itemTonalElevation: Dp = ChipGroup.TonalElevation,
    itemColors: ChipGroup.Colors = ChipGroup.colors(),
    itemTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    spacing: Dp = ChipGroup.Spacing,
) {
    Layout(
        content = {
            items.value.forEach { item ->
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
        },
        modifier = modifier.wrapContentSize(),
    ) { measurables, constraints ->
        var currentRow = 0
        var currentOrigin = IntOffset.Zero
        val spacingValue = spacing.toPx().toInt()
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentOrigin.x > 0f && currentOrigin.x + placeable.width > constraints.maxWidth) {
                currentRow += 1
                currentOrigin = currentOrigin.copy(x = 0, y = currentOrigin.y + placeable.height + spacingValue)
            }

            placeable to currentOrigin.also {
                currentOrigin = it.copy(x = it.x + placeable.width + spacingValue)
            }
        }

        layout(
            width = constraints.maxWidth,
            height = placeables.lastOrNull()?.let { (placeable, origin) -> origin.y + placeable.height } ?: 0,
        ) {
            placeables.forEach { (placeable, origin) -> placeable.place(origin.x, origin.y) }
        }
    }
}

@Composable
fun SingleLineChipGroup(
    items: StableList<ChipGroup.Item>,
    onItemClick: (ChipGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    onItemIconClick: (ChipGroup.Item) -> Unit = onItemClick,
    itemShape: CornerBasedShape = MaterialTheme.shapes.small,
    itemTonalElevation: Dp = ChipGroup.TonalElevation,
    itemColors: ChipGroup.Colors = ChipGroup.colors(),
    itemTextStyle: TextStyle = MaterialTheme.typography.labelMedium,
    spacing: Dp = ChipGroup.Spacing,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        items(items.value) { item ->
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

object ChipGroup {

    internal val MinSize: Dp = 40.dp
    internal val Spacing: Dp = 8.dp
    internal val TonalElevation: Dp = 2.dp

    @Immutable
    data class Item(
        val text: String,
        val icon: Painter = EmptyPainter,
        val isSelected: Boolean = false,
    )

    @Immutable
    data class Colors(
        val selectedChipColor: Color,
        val unselectedChipColor: Color,
        val selectedTextColor: Color,
        val unselectedTextColor: Color,
        val selectedIconColor: Color,
        val unselectedIconColor: Color,
    )

    @Composable
    fun colors(
        selectedChipColor: Color = MaterialTheme.colorScheme.primaryContainer,
        unselectedChipColor: Color = MaterialTheme.colorScheme.surface,
        selectedTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        selectedIconColor: Color = selectedTextColor,
        unselectedIconColor: Color = unselectedTextColor,
    ): Colors = Colors(
        selectedChipColor = selectedChipColor,
        unselectedChipColor = unselectedChipColor,
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        selectedIconColor = selectedIconColor,
        unselectedIconColor = unselectedIconColor,
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
            items = items.toStableList(),
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
            items = items.toStableList(),
            onItemClick = {},
            modifier = Modifier.padding(8.dp),
            spacing = 8.dp,
        )
    }
}
