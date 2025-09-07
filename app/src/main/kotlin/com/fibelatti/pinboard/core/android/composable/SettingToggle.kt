package com.fibelatti.pinboard.core.android.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun SettingToggle(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )

            if (description != null) {
                Text(
                    text = description,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        SwitchWithIcon(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag("setting-toggle-$title"),
        )
    }
}

@Composable
@ThemePreviews
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
