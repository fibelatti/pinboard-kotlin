package com.fibelatti.pinboard.core.android.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.ui.preview.DevicePreviews
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun MainTitle(
    title: MainState.TitleComponent,
    subtitle: MainState.TitleComponent,
    navigation: MainState.NavigationComponent,
    onNavigationClicked: () -> Unit,
    actionButton: MainState.ActionButtonComponent,
    onActionButtonClicked: (data: Any?) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(visible = navigation is MainState.NavigationComponent.Visible) {
            IconButton(onClick = onNavigationClicked) {
                if (navigation is MainState.NavigationComponent.Visible) {
                    Image(
                        painter = painterResource(id = navigation.icon),
                        contentDescription = stringResource(id = R.string.cd_navigate_back),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1F)
                .heightIn(min = 56.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = title is MainState.TitleComponent.Visible,
            ) {
                val label = (title as? MainState.TitleComponent.Visible)?.label ?: ""
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            AnimatedVisibility(
                visible = subtitle is MainState.TitleComponent.Visible,
            ) {
                val label = (subtitle as? MainState.TitleComponent.Visible)?.label ?: ""

                Text(
                    text = label,
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        AnimatedVisibility(
            visible = actionButton is MainState.ActionButtonComponent.Visible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            val label = (actionButton as? MainState.ActionButtonComponent.Visible)?.label ?: ""
            val data = (actionButton as? MainState.ActionButtonComponent.Visible)?.data

            FilledTonalButton(
                onClick = { onActionButtonClicked(data) },
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

// region Previews
@Composable
@ThemePreviews
@DevicePreviews
private fun MainTitlePreview() {
    ExtendedTheme {
        Box {
            MainTitle(
                title = MainState.TitleComponent.Visible("Title"),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(id = "id"),
                onNavigationClicked = {},
                actionButton = MainState.ActionButtonComponent.Visible(id = "id", label = "Action"),
                onActionButtonClicked = {},
            )
        }
    }
}

@Composable
@ThemePreviews
@DevicePreviews
private fun MainTitleWithSubtitlePreview() {
    ExtendedTheme {
        Box {
            MainTitle(
                title = MainState.TitleComponent.Visible("Title"),
                subtitle = MainState.TitleComponent.Visible("Subtitle"),
                navigation = MainState.NavigationComponent.Visible(id = "id"),
                onNavigationClicked = {},
                actionButton = MainState.ActionButtonComponent.Visible(id = "id", label = "Action"),
                onActionButtonClicked = {},
            )
        }
    }
}
// endregion Previews
