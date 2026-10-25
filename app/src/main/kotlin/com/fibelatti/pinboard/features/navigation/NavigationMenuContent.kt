package com.fibelatti.pinboard.features.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.ui.components.AutoSizeText
import com.fibelatti.ui.components.ListItem
import com.fibelatti.ui.foundation.Shapes
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun NavigationMenuContent(
    appMode: AppMode,
    onActionClick: (NavigationMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val serviceName = remember(appMode) {
            when (appMode) {
                AppMode.PINBOARD -> R.string.pinboard
                AppMode.LINKDING -> R.string.linkding
                else -> null
            }
        }

        if (serviceName != null) {
            Text(
                text = stringResource(id = serviceName),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        val groups: List<List<NavigationMenuItem>> = remember(appMode) { navigationMenuGroups(appMode) }

        groups.forEachIndexed { groupIndex: Int, group: List<NavigationMenuItem> ->
            if (groupIndex > 0) {
                Spacer(modifier = Modifier.height(30.dp))
            }

            group.forEachIndexed { index: Int, item: NavigationMenuItem ->
                MenuItem(
                    textRes = item.textRes,
                    onClick = { onActionClick(item.action) },
                    icon = item.icon,
                    shape = when {
                        group.size == 1 -> Shapes.StandaloneShape
                        index == 0 -> Shapes.TopShape
                        index == group.lastIndex -> Shapes.BottomShape
                        else -> Shapes.MiddleShape
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AppVersionDetails(
            onClick = { onActionClick(NavigationMenuAction.ViewLicenses) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AppVersionDetails(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 8.dp),
        horizontalAlignment = horizontalAlignment,
    ) {
        AutoSizeText(
            text = stringResource(id = R.string.about_developer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            maxLines = 2,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )

        Text(
            text = stringResource(id = R.string.about_oss_licenses),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun MenuItem(
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    shape: Shape = Shapes.StandaloneShape,
) {
    ListItem(
        headlineText = stringResource(id = textRes),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick, role = Role.Button),
        leadingContent = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(id = textRes),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        shape = shape,
    )
}

@Composable
@PreviewAll
private fun NavigationMenuContentPreview() {
    ExtendedTheme {
        NavigationMenuContent(
            appMode = AppMode.PINBOARD,
            onActionClick = {},
        )
    }
}
