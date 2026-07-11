package com.fibelatti.pinboard.core.android.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Check
import com.fibelatti.pinboard.core.android.icons.Xmark
import com.fibelatti.ui.components.ListItem
import com.fibelatti.ui.foundation.Shapes
import com.fibelatti.ui.preview.PreviewAccessibility
import com.fibelatti.ui.preview.PreviewThemesAndColors
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun SettingToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    shape: Shape = Shapes.StandaloneShape,
) {
    ListItem(
        headlineText = title,
        modifier = modifier,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag("setting-toggle-$title"),
                thumbContent = {
                    val icon: ImageVector = if (checked) AppIcons.Check else AppIcons.Xmark

                    AnimatedContent(
                        targetState = icon,
                        transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                    ) { vector ->
                        Icon(
                            imageVector = vector,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = if (checked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.inverseOnSurface
                            },
                        )
                    }
                },
            )
        },
        supportingText = description,
        shape = shape,
    )
}

@Composable
@PreviewThemesAndColors
@PreviewAccessibility
private fun SettingTogglePreview() {
    ExtendedTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingToggle(
                title = "Setting - enabled",
                description = "Setting description",
                checked = true,
                onCheckedChange = {},
            )

            SettingToggle(
                title = "Setting - disabled",
                description = "Setting description",
                checked = false,
                onCheckedChange = {},
            )

            SettingToggle(
                title = "Setting - no description",
                description = null,
                checked = true,
                onCheckedChange = {},
            )
        }
    }
}
