@file:Suppress("LongMethod")

package com.fibelatti.pinboard.core.android.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun MainTitle(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()

    MainTitle(
        title = state.title,
        subtitle = state.subtitle,
        navigation = state.navigation,
        onNavigationClicked = { mainViewModel.navigationClicked(state.navigation.id) },
        actionButton = state.actionButton,
        onActionButtonClicked = { data -> mainViewModel.actionButtonClicked(state.actionButton.id, data) }
    )
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun MainTitle(
    title: MainState.TitleComponent = MainState.TitleComponent.Gone,
    subtitle: MainState.TitleComponent = MainState.TitleComponent.Gone,
    navigation: MainState.NavigationComponent = MainState.NavigationComponent.Gone,
    onNavigationClicked: () -> Unit = {},
    actionButton: MainState.ActionButtonComponent = MainState.ActionButtonComponent.Gone,
    onActionButtonClicked: (data: Any?) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(visible = navigation is MainState.NavigationComponent.Visible) {
            IconButton(onClick = onNavigationClicked) {
                if (navigation is MainState.NavigationComponent.Visible) {
                    Image(
                        painter = painterResource(id = navigation.icon),
                        contentDescription = stringResource(id = R.string.cd_navigate_back)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1F)
                .height(65.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = title is MainState.TitleComponent.Visible,
            ) {
                if (title is MainState.TitleComponent.Visible) {
                    Text(
                        text = title.label,
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = ExtendedTheme.typography.title,
                    )
                }
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
                    style = ExtendedTheme.typography.detail,
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
                    style = ExtendedTheme.typography.caveat,
                )
            }
        }
    }
}

@Composable
@ThemePreviews
private fun MainTitlePreview() {
    ExtendedTheme {
        Box {
            MainTitle(
                title = MainState.TitleComponent.Visible("Title"),
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(id = "id"),
                actionButton = MainState.ActionButtonComponent.Visible(id = "id", label = "Action"),
            )
        }
    }
}

@Composable
@ThemePreviews
private fun MainTitleWithSubtitlePreview() {
    ExtendedTheme {
        Box {
            MainTitle(
                title = MainState.TitleComponent.Visible("Title"),
                subtitle = MainState.TitleComponent.Visible("Subtitle"),
                navigation = MainState.NavigationComponent.Visible(id = "id"),
                actionButton = MainState.ActionButtonComponent.Visible(id = "id", label = "Action"),
            )
        }
    }
}
