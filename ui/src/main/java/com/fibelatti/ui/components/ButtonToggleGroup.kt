package com.fibelatti.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fibelatti.ui.components.ToggleButtonGroup.SquareCorner
import com.fibelatti.ui.foundation.StableList
import com.fibelatti.ui.foundation.toStableList
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun ColumnToggleButtonGroup(
    items: StableList<ToggleButtonGroup.Item>,
    onButtonClick: (ToggleButtonGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    selectedIndex: Int = -1,
    enabled: Boolean = true,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    colors: ToggleButtonGroup.Colors = ToggleButtonGroup.colors(),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(),
    borderSize: Dp = ToggleButtonGroup.BorderSize,
    border: BorderStroke = BorderStroke(borderSize, colors.borderColor),
    buttonHeight: Dp = ToggleButtonGroup.ButtonHeight,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    iconPosition: ToggleButtonGroup.IconPosition = ToggleButtonGroup.IconPosition.Start,
) {
    Column(modifier = modifier) {
        val mode = when {
            items.value.all { it.text != "" && it.icon == EmptyPainter } -> ToggleButtonGroup.Mode.TextOnly
            items.value.all { it.text == "" && it.icon != EmptyPainter } -> ToggleButtonGroup.Mode.IconOnly
            else -> ToggleButtonGroup.Mode.TextAndIcon
        }

        items.value.forEachIndexed { index, toggleButtonGroupItem ->
            val isButtonSelected = selectedIndex == index

            ToggleButton(
                item = toggleButtonGroupItem,
                mode = mode,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = buttonHeight)
                    .offset(y = borderSize * -index),
                buttonShape = when (index) {
                    0 -> shape.copy(bottomStart = SquareCorner, bottomEnd = SquareCorner)
                    items.value.size - 1 -> shape.copy(topStart = SquareCorner, topEnd = SquareCorner)
                    else -> shape.copy(all = SquareCorner)
                },
                border = border,
                containerColor = if (isButtonSelected) colors.selectedButtonColor else colors.unselectedButtonColor,
                elevation = elevation,
                enabled = enabled,
                textColor = if (isButtonSelected) colors.selectedTextColor else colors.unselectedTextColor,
                textStyle = textStyle,
                iconColor = if (isButtonSelected) colors.selectedIconColor else colors.unselectedIconColor,
                iconPosition = iconPosition,
                onClick = { onButtonClick(toggleButtonGroupItem) },
            )
        }
    }
}

@Composable
fun RowToggleButtonGroup(
    items: StableList<ToggleButtonGroup.Item>,
    onButtonClick: (ToggleButtonGroup.Item) -> Unit,
    modifier: Modifier = Modifier,
    selectedIndex: Int = -1,
    enabled: Boolean = true,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    colors: ToggleButtonGroup.Colors = ToggleButtonGroup.colors(),
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(),
    borderSize: Dp = ToggleButtonGroup.BorderSize,
    border: BorderStroke = BorderStroke(borderSize, colors.borderColor),
    buttonHeight: Dp = ToggleButtonGroup.ButtonHeight,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    iconPosition: ToggleButtonGroup.IconPosition = ToggleButtonGroup.IconPosition.Start,
) {
    Row(modifier = modifier) {
        val squareCorner = CornerSize(0.dp)
        val mode = when {
            items.value.all { it.text != "" && it.icon == EmptyPainter } -> ToggleButtonGroup.Mode.TextOnly
            items.value.all { it.text == "" && it.icon != EmptyPainter } -> ToggleButtonGroup.Mode.IconOnly
            else -> ToggleButtonGroup.Mode.TextOnly
        }

        items.value.forEachIndexed { index, toggleButtonGroupItem ->
            val isButtonSelected = selectedIndex == index

            ToggleButton(
                item = toggleButtonGroupItem,
                mode = mode,
                modifier = Modifier
                    .weight(weight = 1f)
                    .defaultMinSize(minHeight = buttonHeight)
                    .offset(x = borderSize * -index),
                buttonShape = when (index) {
                    0 -> shape.copy(bottomEnd = squareCorner, topEnd = squareCorner)
                    items.value.size - 1 -> shape.copy(topStart = squareCorner, bottomStart = squareCorner)
                    else -> shape.copy(all = squareCorner)
                },
                border = border,
                containerColor = if (isButtonSelected) colors.selectedButtonColor else colors.unselectedButtonColor,
                elevation = elevation,
                enabled = enabled,
                textColor = if (isButtonSelected) colors.selectedTextColor else colors.unselectedTextColor,
                textStyle = textStyle,
                iconColor = if (isButtonSelected) colors.selectedIconColor else colors.unselectedIconColor,
                iconPosition = iconPosition,
                onClick = { onButtonClick.invoke(toggleButtonGroupItem) },
            )
        }
    }
}

@Composable
private fun ToggleButton(
    item: ToggleButtonGroup.Item,
    mode: ToggleButtonGroup.Mode,
    modifier: Modifier,
    buttonShape: CornerBasedShape,
    border: BorderStroke,
    containerColor: Color,
    elevation: ButtonElevation,
    enabled: Boolean,
    textColor: Color,
    textStyle: TextStyle,
    iconColor: Color,
    iconPosition: ToggleButtonGroup.IconPosition,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonShape,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
        elevation = elevation,
        border = border,
        contentPadding = ToggleButtonGroup.ButtonPaddingValues,
    ) {
        ButtonContent(
            item = item,
            mode = mode,
            textColor = textColor,
            textStyle = textStyle,
            iconColor = iconColor,
            iconPosition = iconPosition,
        )
    }
}

@Composable
private fun RowScope.ButtonContent(
    item: ToggleButtonGroup.Item,
    mode: ToggleButtonGroup.Mode,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = LocalTextStyle.current,
    iconColor: Color = Color.Unspecified,
    iconPosition: ToggleButtonGroup.IconPosition = ToggleButtonGroup.IconPosition.Start,
) {
    when (mode) {
        ToggleButtonGroup.Mode.TextOnly -> TextContent(
            item = item,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = textColor,
            style = textStyle,
        )

        ToggleButtonGroup.Mode.IconOnly -> IconContent(
            item = item,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = iconColor,
        )

        ToggleButtonGroup.Mode.TextAndIcon -> ButtonWithIconAndText(
            item = item,
            textColor = textColor,
            textStyle = textStyle,
            iconColor = iconColor,
            iconPosition = iconPosition,
        )
    }
}

@Composable
private fun RowScope.ButtonWithIconAndText(
    item: ToggleButtonGroup.Item,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = LocalTextStyle.current,
    iconColor: Color = Color.Unspecified,
    iconPosition: ToggleButtonGroup.IconPosition = ToggleButtonGroup.IconPosition.Start,
) {
    when (iconPosition) {
        ToggleButtonGroup.IconPosition.Start -> {
            IconContent(item = item, modifier = Modifier.align(Alignment.CenterVertically), color = iconColor)
            TextContent(
                item = item,
                modifier = Modifier.align(Alignment.CenterVertically),
                color = textColor,
                style = textStyle,
            )
        }

        ToggleButtonGroup.IconPosition.Top -> Column {
            IconContent(item = item, modifier = Modifier.align(Alignment.CenterHorizontally), color = iconColor)
            TextContent(
                item = item,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = textColor,
                style = textStyle,
            )
        }

        ToggleButtonGroup.IconPosition.End -> {
            TextContent(
                item = item,
                modifier = Modifier.align(Alignment.CenterVertically),
                color = textColor,
                style = textStyle,
            )
            IconContent(item = item, modifier = Modifier.align(Alignment.CenterVertically), color = iconColor)
        }

        ToggleButtonGroup.IconPosition.Bottom -> Column {
            TextContent(
                item = item,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = textColor,
                style = textStyle,
            )
            IconContent(item = item, modifier = Modifier.align(Alignment.CenterHorizontally), color = iconColor)
        }
    }
}

@Composable
private fun IconContent(
    item: ToggleButtonGroup.Item,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Image(
        painter = item.icon,
        contentDescription = null,
        modifier = modifier.size(24.dp),
        colorFilter = ColorFilter.tint(color).takeUnless { color == Color.Transparent || color == Color.Unspecified },
    )
}

@Composable
private fun TextContent(
    item: ToggleButtonGroup.Item,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = item.text,
        modifier = modifier.padding(horizontal = 8.dp),
        color = color,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = style,
    )
}

object ToggleButtonGroup {

    internal val BorderSize: Dp = 1.dp
    internal val ButtonHeight: Dp = 48.dp
    internal val ButtonPaddingValues = PaddingValues()
    internal val SquareCorner = CornerSize(0.dp)

    @Immutable
    data class Item(
        val id: String,
        val text: String,
        val icon: Painter = EmptyPainter,
    )

    @Immutable
    data class Colors(
        val selectedButtonColor: Color,
        val unselectedButtonColor: Color,
        val selectedTextColor: Color,
        val unselectedTextColor: Color,
        val selectedIconColor: Color,
        val unselectedIconColor: Color,
        val borderColor: Color,
    )

    enum class IconPosition {
        Start, Top, End, Bottom
    }

    internal enum class Mode {
        TextOnly, IconOnly, TextAndIcon,
    }

    @Composable
    fun colors(
        selectedButtonColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        unselectedButtonColor: Color = MaterialTheme.colorScheme.background,
        selectedTextColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        unselectedTextColor: Color = MaterialTheme.colorScheme.onBackground,
        selectedIconColor: Color = selectedTextColor,
        unselectedIconColor: Color = unselectedTextColor,
        borderColor: Color = selectedButtonColor,
    ): Colors = Colors(
        selectedButtonColor = selectedButtonColor,
        unselectedButtonColor = unselectedButtonColor,
        selectedTextColor = selectedTextColor,
        unselectedTextColor = unselectedTextColor,
        selectedIconColor = selectedIconColor,
        unselectedIconColor = unselectedIconColor,
        borderColor = borderColor,
    )
}

@Composable
@ThemePreviews
private fun RowToggleButtonGroupPreview() {
    ExtendedTheme {
        val items = List(4) {
            ToggleButtonGroup.Item(
                id = "$it",
                text = "Button $it",
            )
        }

        RowToggleButtonGroup(
            items = items.toStableList(),
            onButtonClick = {},
            selectedIndex = 1,
        )
    }
}
