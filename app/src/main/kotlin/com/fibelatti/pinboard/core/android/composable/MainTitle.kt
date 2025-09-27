@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.core.android.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.ui.components.AutoSizeText
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .fillMaxWidth()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var navigationIconRes: Int by remember { mutableIntStateOf(-1) }
        RememberedEffect(navigation) {
            if (navigation is MainState.NavigationComponent.Visible) {
                navigationIconRes = navigation.icon
            }
        }

        AnimatedVisibility(visible = navigation is MainState.NavigationComponent.Visible && navigationIconRes != -1) {
            LongClickIconButton(
                painter = painterResource(id = navigationIconRes),
                description = stringResource(id = R.string.cd_navigate_back),
                onClick = onNavigationClicked,
                iconTint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 64.dp)
                .padding(
                    start = if (navigation is MainState.NavigationComponent.Visible) 0.dp else 16.dp,
                    end = 16.dp,
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            var titleText by remember { mutableStateOf("") }
            RememberedEffect(title) {
                if (title is MainState.TitleComponent.Visible) {
                    titleText = title.label
                }
            }

            var subtitleText by remember { mutableStateOf("") }
            RememberedEffect(subtitle) {
                if (subtitle is MainState.TitleComponent.Visible) {
                    subtitleText = subtitle.label
                }
            }

            AnimatedVisibility(
                visible = title is MainState.TitleComponent.Visible && titleText.isNotEmpty(),
            ) {
                Text(
                    text = titleText,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            val colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

            AnimatedVisibility(
                visible = subtitle is MainState.TitleComponent.Visible && subtitleText.isNotEmpty(),
            ) {
                AutoSizeText(
                    text = subtitleText,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(
                            color = colors.toolbarContainerColor,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = colors.toolbarContentColor,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        var actionButtonText by remember { mutableStateOf("") }
        RememberedEffect(actionButton) {
            if (actionButton is MainState.ActionButtonComponent.Visible) {
                actionButtonText = actionButton.label
            }
        }
        val actionButtonData by rememberUpdatedState((actionButton as? MainState.ActionButtonComponent.Visible)?.data)

        AnimatedVisibility(
            visible = actionButton is MainState.ActionButtonComponent.Visible && actionButtonText.isNotEmpty(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            FilledTonalButton(
                onClick = { onActionButtonClicked(actionButtonData) },
                shapes = ExtendedTheme.defaultButtonShapes,
                modifier = Modifier.padding(end = 16.dp),
            ) {
                Text(
                    text = actionButtonText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
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
                navigation = MainState.NavigationComponent.Visible(),
                onNavigationClicked = {},
                actionButton = MainState.ActionButtonComponent.Visible(contentType = Content::class, label = "Action"),
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
                navigation = MainState.NavigationComponent.Visible(),
                onNavigationClicked = {},
                actionButton = MainState.ActionButtonComponent.Visible(contentType = Content::class, label = "Action"),
                onActionButtonClicked = {},
            )
        }
    }
}
// endregion Previews
