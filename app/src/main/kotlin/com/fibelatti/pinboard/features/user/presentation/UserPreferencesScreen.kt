package com.fibelatti.pinboard.features.user.presentation

import android.os.Build
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.composable.SettingToggle
import com.fibelatti.pinboard.core.extension.fillWidthOfParent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.PostQuickActions
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagManager
import com.fibelatti.pinboard.features.tags.presentation.TagManagerViewModel
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.launch

@Composable
fun UserPreferencesScreen(
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    onDynamicColorChange: () -> Unit,
    onDisableScreenshotsChange: () -> Unit,
) {
    val appMode by appStateViewModel.appMode.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .background(color = ExtendedTheme.colors.backgroundNoOverlay)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, bottom = 32.dp)
            .windowInsetsPadding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
    ) {
        if (maxWidth < 600.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppPreferencesContent(
                    appMode = appMode,
                    onDynamicColorChange = onDynamicColorChange,
                    onDisableScreenshotsChange = onDisableScreenshotsChange,
                )

                BookmarkingPreferencesContent(
                    appMode = appMode,
                    modifier = Modifier.padding(top = 32.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val childWidth = this@BoxWithConstraints.maxWidth / 2

                AppPreferencesContent(
                    appMode = appMode,
                    onDynamicColorChange = onDynamicColorChange,
                    onDisableScreenshotsChange = onDisableScreenshotsChange,
                    modifier = Modifier.requiredWidth(childWidth),
                )

                BookmarkingPreferencesContent(
                    appMode = appMode,
                    modifier = Modifier.requiredWidth(childWidth),
                )
            }
        }
    }
}

@Composable
private fun AppPreferencesContent(
    appMode: AppMode,
    onDynamicColorChange: () -> Unit,
    onDisableScreenshotsChange: () -> Unit,
    modifier: Modifier = Modifier,
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
) {
    val userPreferences by userPreferencesViewModel.currentPreferences.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    AppPreferencesContent(
        appMode = appMode,
        userPreferences = userPreferences,
        onAppearanceChange = { newAppearance ->
            userPreferencesViewModel.saveAppearance(newAppearance)

            scope.launch {
                val mode = when (newAppearance) {
                    is Appearance.DarkTheme -> AppCompatDelegate.MODE_NIGHT_YES
                    is Appearance.LightTheme -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                AppCompatDelegate.setDefaultNightMode(mode)
            }
        },
        onDynamicColorChange = { newValue: Boolean ->
            userPreferencesViewModel.saveApplyDynamicColors(newValue)
            onDynamicColorChange()
        },
        onDisableScreenshotsChange = { newValue: Boolean ->
            userPreferencesViewModel.saveDisableScreenshots(newValue)
            onDisableScreenshotsChange()
        },
        onDateFormatChange = userPreferencesViewModel::savePreferredDateFormat,
        onPeriodicSyncChange = userPreferencesViewModel::savePeriodicSync,
        onHiddenOptionsChange = userPreferencesViewModel::saveHiddenPostQuickOptions,
        onPreferredViewChange = userPreferencesViewModel::savePreferredDetailsView,
        onAlwaysUseSidePanelChange = userPreferencesViewModel::saveAlwaysUseSidePanel,
        onMarkAsReadOnOpenChange = userPreferencesViewModel::saveMarkAsReadOnOpen,
        onShowDescriptionInListsChange = userPreferencesViewModel::saveShowDescriptionInLists,
        modifier = modifier,
    )
}

@Composable
private fun AppPreferencesContent(
    appMode: AppMode,
    userPreferences: UserPreferences,
    onAppearanceChange: (Appearance) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDisableScreenshotsChange: (Boolean) -> Unit,
    onDateFormatChange: (PreferredDateFormat) -> Unit,
    onPeriodicSyncChange: (PeriodicSync) -> Unit,
    onHiddenOptionsChange: (Set<String>) -> Unit,
    onPreferredViewChange: (PreferredDetailsView) -> Unit,
    onAlwaysUseSidePanelChange: (Boolean) -> Unit,
    onMarkAsReadOnOpenChange: (Boolean) -> Unit,
    onShowDescriptionInListsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.user_preferences_section_app),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge,
        )

        if (AppMode.LINKDING == appMode) {
            Text(
                text = stringResource(
                    R.string.user_preferences_linkding_connection_description,
                    userPreferences.linkdingInstanceUrl,
                ),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(all = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingItem(
            title = stringResource(id = R.string.user_preferences_appearance),
        ) {
            PreferenceSelectionButton(
                currentSelection = userPreferences.appearance,
                buttonText = { option: Appearance ->
                    when (option) {
                        Appearance.DarkTheme -> R.string.user_preferences_appearance_dark
                        Appearance.LightTheme -> R.string.user_preferences_appearance_light
                        Appearance.SystemDefault -> R.string.user_preferences_appearance_system_default
                    }
                },
                title = R.string.user_preferences_appearance,
                options = {
                    listOf(
                        Appearance.DarkTheme,
                        Appearance.LightTheme,
                        Appearance.SystemDefault,
                    )
                },
                onOptionSelected = onAppearanceChange,
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_dynamic_colors),
                description = stringResource(id = R.string.user_preferences_dynamic_colors_caveat),
                checked = userPreferences.applyDynamicColors,
                onCheckedChange = onDynamicColorChange,
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_disable_screenshots),
            description = stringResource(id = R.string.user_preferences_disable_screenshots_caveat),
            checked = userPreferences.disableScreenshots,
            onCheckedChange = onDisableScreenshotsChange,
        )

        SettingItem(
            title = stringResource(id = R.string.user_preferences_date_format),
        ) {
            PreferenceSelectionButton(
                currentSelection = userPreferences.preferredDateFormat,
                buttonText = { option: PreferredDateFormat ->
                    when (option) {
                        PreferredDateFormat.DayMonthYearWithTime -> R.string.user_preferences_date_format_day_first
                        PreferredDateFormat.MonthDayYearWithTime -> R.string.user_preferences_date_format_month_first
                        PreferredDateFormat.ShortYearMonthDayWithTime ->
                            R.string.user_preferences_date_format_short_year_first

                        PreferredDateFormat.YearMonthDayWithTime -> R.string.user_preferences_date_format_year_first
                    }
                },
                title = R.string.user_preferences_date_format,
                options = {
                    listOf(
                        PreferredDateFormat.DayMonthYearWithTime,
                        PreferredDateFormat.MonthDayYearWithTime,
                        PreferredDateFormat.ShortYearMonthDayWithTime,
                        PreferredDateFormat.YearMonthDayWithTime,
                    )
                },
                onOptionSelected = onDateFormatChange,
            )
        }

        if (AppMode.NO_API != appMode) {
            SettingItem(
                title = stringResource(id = R.string.user_preferences_periodic_sync),
            ) {
                Text(
                    text = stringResource(id = R.string.user_preferences_periodic_sync_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                PreferenceSelectionButton(
                    currentSelection = userPreferences.periodicSync,
                    buttonText = { option: PeriodicSync ->
                        when (option) {
                            PeriodicSync.Off -> R.string.user_preferences_periodic_sync_off
                            PeriodicSync.Every6Hours -> R.string.user_preferences_periodic_sync_6_hours
                            PeriodicSync.Every12Hours -> R.string.user_preferences_periodic_sync_12_hours
                            PeriodicSync.Every24Hours -> R.string.user_preferences_periodic_sync_24_hours
                        }
                    },
                    title = R.string.user_preferences_periodic_sync,
                    options = {
                        listOf(
                            PeriodicSync.Off,
                            PeriodicSync.Every6Hours,
                            PeriodicSync.Every12Hours,
                            PeriodicSync.Every24Hours,
                        )
                    },
                    onOptionSelected = onPeriodicSyncChange,
                )

                Text(
                    text = stringResource(id = R.string.user_preferences_periodic_sync_caveat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SettingItem(
            title = stringResource(R.string.user_preferences_bookmark_quick_options),
        ) {
            Text(
                text = stringResource(R.string.user_preferences_bookmark_quick_actions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val localContext = LocalContext.current
            PreferenceButton(
                buttonText = stringResource(R.string.user_preferences_customize),
                onClick = {
                    val samplePost = Post.EMPTY.copy(
                        description = "sample_description",
                        tags = listOf(Tag(name = "sample_tags")),
                    )
                    val allOptions = PostQuickActions.allOptions(samplePost)
                        .associateWith { option -> option.serializedName in userPreferences.hiddenPostQuickOptions }

                    SelectionDialog.showCustomizationDialog(
                        context = localContext,
                        title = localContext.getString(R.string.user_preferences_bookmark_quick_options),
                        options = allOptions,
                        optionName = { option -> localContext.getString(option.title) },
                        optionIcon = PostQuickActions::icon,
                        onConfirm = { options ->
                            val hiddenOptions = options.filterValues { hidden -> hidden }.keys
                                .map { it.serializedName }
                                .toSet()

                            onHiddenOptionsChange(hiddenOptions)
                        },
                    )
                },
            )
        }

        val markAsReadOnOpen by rememberUpdatedState(
            newValue = when (val pdv = userPreferences.preferredDetailsView) {
                is PreferredDetailsView.ExternalBrowser -> pdv.markAsReadOnOpen
                is PreferredDetailsView.InAppBrowser -> pdv.markAsReadOnOpen
                is PreferredDetailsView.Edit -> false
            },
        )

        SettingItem(
            stringResource(id = R.string.user_preferences_preferred_details_view),
        ) {
            PreferenceSelectionButton(
                currentSelection = userPreferences.preferredDetailsView,
                buttonText = { option: PreferredDetailsView ->
                    when (option) {
                        is PreferredDetailsView.InAppBrowser -> {
                            R.string.user_preferences_preferred_details_in_app_browser
                        }

                        is PreferredDetailsView.ExternalBrowser -> {
                            R.string.user_preferences_preferred_details_external_browser
                        }

                        is PreferredDetailsView.Edit -> {
                            R.string.user_preferences_preferred_details_post_details
                        }
                    }
                },
                title = R.string.user_preferences_preferred_details_view,
                options = {
                    listOf(
                        PreferredDetailsView.InAppBrowser(markAsReadOnOpen),
                        PreferredDetailsView.ExternalBrowser(markAsReadOnOpen),
                        PreferredDetailsView.Edit,
                    )
                },
                onOptionSelected = onPreferredViewChange,
            )

            Text(
                text = stringResource(id = R.string.user_preferences_preferred_details_view_caveat),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = userPreferences.preferredDetailsView !is PreferredDetailsView.Edit,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_preferred_details_view_mark_as_read_on_open),
                description = stringResource(
                    id = R.string.user_preferences_preferred_details_view_mark_as_read_on_open_caveat,
                ),
                checked = markAsReadOnOpen,
                onCheckedChange = onMarkAsReadOnOpenChange,
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_always_use_side_panel),
            description = stringResource(id = R.string.user_preferences_always_use_side_panel_description),
            checked = userPreferences.alwaysUseSidePanel,
            onCheckedChange = onAlwaysUseSidePanelChange,
        )

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_description_visible_in_lists),
            description = stringResource(id = R.string.user_preferences_description_visible_in_lists_description),
            checked = userPreferences.showDescriptionInLists,
            onCheckedChange = onShowDescriptionInListsChange,
        )
    }
}

@Composable
private fun BookmarkingPreferencesContent(
    appMode: AppMode,
    modifier: Modifier = Modifier,
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
    tagManagerViewModel: TagManagerViewModel = hiltViewModel(),
) {
    val userPreferences by userPreferencesViewModel.currentPreferences.collectAsStateWithLifecycle()
    val suggestedTags by userPreferencesViewModel.suggestedTags.collectAsStateWithLifecycle(emptyList())
    val tagState by tagManagerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        tagManagerViewModel.initializeTags(userPreferences.defaultTags)
    }

    LaunchedEffect(suggestedTags) {
        tagManagerViewModel.setSuggestedTags(suggestedTags)
    }

    LaunchedEffect(tagState) {
        userPreferencesViewModel.saveDefaultTags(tagState.tags)
        userPreferencesViewModel.searchForTag(tagState.currentQuery, tagState.tags)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BookmarkingPreferencesContent(
            appMode = appMode,
            userPreferences = userPreferences,
            onEditAfterSharingChange = userPreferencesViewModel::saveEditAfterSharing,
            onFollowRedirectsChange = userPreferencesViewModel::saveFollowRedirects,
            onRemoveUtmParametersChange = userPreferencesViewModel::saveRemoveUtmParameters,
            onRemovedUrlParametersChange = userPreferencesViewModel::saveRemovedUrlParameters,
            onAutoFillDescriptionChange = userPreferencesViewModel::saveAutoFillDescription,
            onUseBlockquoteChange = userPreferencesViewModel::saveUseBlockquote,
            onPrivateByDefaultChange = userPreferencesViewModel::saveDefaultPrivate,
            onReadLaterByDefaultChange = userPreferencesViewModel::saveDefaultReadLater,
        )

        SettingItem(
            title = stringResource(id = R.string.user_preferences_default_tags),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .animateContentSize(),
        ) {
            Text(
                text = stringResource(id = R.string.user_preferences_default_tags_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TagManager(
                searchTagInput = tagState.currentQuery,
                onSearchTagInputChanged = tagManagerViewModel::setQuery,
                onAddTagClicked = tagManagerViewModel::addTag,
                suggestedTags = tagState.suggestedTags,
                onSuggestedTagClicked = tagManagerViewModel::addTag,
                currentTagsTitle = stringResource(id = tagState.displayTitle),
                currentTags = tagState.tags,
                onRemoveCurrentTagClicked = tagManagerViewModel::removeTag,
                modifier = Modifier.fillWidthOfParent(parentPaddingStart = 8.dp, parentPaddingEnd = 8.dp),
                horizontalPadding = 8.dp,
            )
        }
    }
}

@Composable
private fun BookmarkingPreferencesContent(
    appMode: AppMode,
    userPreferences: UserPreferences,
    onEditAfterSharingChange: (EditAfterSharing) -> Unit,
    onFollowRedirectsChange: (Boolean) -> Unit,
    onRemoveUtmParametersChange: (Boolean) -> Unit,
    onRemovedUrlParametersChange: (Set<String>) -> Unit,
    onAutoFillDescriptionChange: (Boolean) -> Unit,
    onUseBlockquoteChange: (Boolean) -> Unit,
    onPrivateByDefaultChange: (Boolean) -> Unit,
    onReadLaterByDefaultChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.user_preferences_section_bookmarking),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge,
        )

        SettingItem(
            title = stringResource(id = R.string.user_preferences_edit_after_sharing_title),
        ) {
            PreferenceSelectionButton(
                currentSelection = userPreferences.editAfterSharing,
                buttonText = { option: EditAfterSharing ->
                    when (option) {
                        is EditAfterSharing.BeforeSaving -> R.string.user_preferences_edit_after_sharing_before_saving
                        is EditAfterSharing.AfterSaving -> R.string.user_preferences_edit_after_sharing_after_saving
                    }
                },
                title = R.string.user_preferences_edit_after_sharing_title,
                options = {
                    listOf(
                        EditAfterSharing.BeforeSaving,
                        EditAfterSharing.AfterSaving,
                    )
                },
                onOptionSelected = onEditAfterSharingChange,
            )

            Text(
                text = stringResource(id = R.string.user_preferences_edit_after_sharing_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_follow_redirects),
            description = stringResource(id = R.string.user_preferences_follow_redirects_description),
            checked = userPreferences.followRedirects,
            onCheckedChange = onFollowRedirectsChange,
        )

        RemoveUrlParametersSetting(
            removeUtmParameters = userPreferences.removeUtmParameters,
            onRemoveUtmParametersChange = onRemoveUtmParametersChange,
            removedParameters = userPreferences.removedUrlParameters,
            onRemovedParametersChange = onRemovedUrlParametersChange,
        )

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_description_auto_fill),
            description = stringResource(id = R.string.user_preferences_description_auto_fill_description),
            checked = userPreferences.autoFillDescription,
            onCheckedChange = onAutoFillDescriptionChange,
        )

        AnimatedVisibility(
            visible = userPreferences.autoFillDescription,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_use_blockquote),
                description = stringResource(id = R.string.user_preferences_use_blockquote_description),
                checked = userPreferences.useBlockquote,
                onCheckedChange = onUseBlockquoteChange,
            )
        }

        if (AppMode.NO_API != appMode) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_default_private_label),
                description = stringResource(id = R.string.user_preferences_default_private_description),
                checked = userPreferences.defaultPrivate,
                onCheckedChange = onPrivateByDefaultChange,
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_default_read_later_label),
            description = stringResource(id = R.string.user_preferences_default_read_later_description),
            checked = userPreferences.defaultReadLater,
            onCheckedChange = onReadLaterByDefaultChange,
        )
    }
}

@Composable
private fun RemoveUrlParametersSetting(
    removeUtmParameters: Boolean,
    onRemoveUtmParametersChange: (Boolean) -> Unit,
    removedParameters: Set<String>,
    onRemovedParametersChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SettingToggle(
            title = stringResource(R.string.user_preferences_remove_utm_parameters),
            description = stringResource(R.string.user_preferences_remove_utm_parameters_description),
            checked = removeUtmParameters,
            onCheckedChange = onRemoveUtmParametersChange,
        )

        SettingItem(
            title = stringResource(R.string.user_preferences_remove_url_parameters),
            modifier = Modifier.animateContentSize(),
        ) {
            Text(
                text = stringResource(R.string.user_preferences_remove_url_parameters_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            var inputValue by remember { mutableStateOf("") }
            val submitValueAction by rememberUpdatedState {
                if (inputValue.isNotBlank()) {
                    onRemovedParametersChange(removedParameters + inputValue)
                }
                inputValue = ""
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { newValue -> inputValue = newValue },
                    modifier = Modifier
                        .weight(1f)
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                                submitValueAction()
                                return@onKeyEvent true
                            }
                            false
                        },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    label = { Text(text = stringResource(R.string.user_preferences_remove_url_parameters_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions { submitValueAction() },
                    singleLine = true,
                    maxLines = 1,
                )

                FilledTonalButton(
                    onClick = submitValueAction,
                    modifier = Modifier.padding(bottom = 4.dp),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(text = stringResource(R.string.hint_add))
                }
            }

            val closeIcon = painterResource(id = R.drawable.ic_close)
            SingleLineChipGroup(
                items = remember(removedParameters) {
                    removedParameters.map { parameter -> ChipGroup.Item(text = parameter, icon = closeIcon) }
                },
                onItemClick = { item -> onRemovedParametersChange(removedParameters - item.text) },
                modifier = Modifier
                    .fillWidthOfParent(parentPaddingStart = 12.dp, parentPaddingEnd = 12.dp)
                    .fillMaxWidth(),
                itemTextStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.SansSerif,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            )
        }
    }
}

// region Components
@Composable
private fun SettingItem(
    title: String,
    modifier: Modifier = Modifier,
    body: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        body()
    }
}

@Composable
private fun PreferenceButton(
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = buttonText,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(4.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_edit),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun <T> PreferenceSelectionButton(
    currentSelection: T,
    buttonText: (T) -> Int,
    @StringRes title: Int,
    options: () -> List<T>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val localContext = LocalContext.current

    PreferenceButton(
        buttonText = stringResource(buttonText(currentSelection)),
        onClick = {
            SelectionDialog.show(
                context = localContext,
                title = localContext.getString(title),
                options = options(),
                optionName = { option -> localContext.getString(buttonText(option)) },
                onOptionSelected = onOptionSelected,
            )
        },
        modifier = modifier,
    )
}
// endregion Components

// region Previews
@Composable
@ThemePreviews
private fun AppPreferencesContentPreview(
    @PreviewParameter(UserPreferencesProvider::class) userPreferences: UserPreferences,
) {
    ExtendedTheme {
        AppPreferencesContent(
            appMode = AppMode.PINBOARD,
            userPreferences = userPreferences,
            onAppearanceChange = {},
            onDynamicColorChange = {},
            onDisableScreenshotsChange = {},
            onDateFormatChange = {},
            onHiddenOptionsChange = {},
            onPeriodicSyncChange = {},
            onPreferredViewChange = {},
            onAlwaysUseSidePanelChange = {},
            onMarkAsReadOnOpenChange = {},
            onShowDescriptionInListsChange = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@Composable
@ThemePreviews
private fun BookmarkingPreferencesContentPreview(
    @PreviewParameter(UserPreferencesProvider::class) userPreferences: UserPreferences,
) {
    ExtendedTheme {
        BookmarkingPreferencesContent(
            appMode = AppMode.PINBOARD,
            userPreferences = userPreferences,
            onEditAfterSharingChange = {},
            onFollowRedirectsChange = {},
            onRemoveUtmParametersChange = {},
            onRemovedUrlParametersChange = {},
            onAutoFillDescriptionChange = {},
            onUseBlockquoteChange = {},
            onPrivateByDefaultChange = {},
            onReadLaterByDefaultChange = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}
// endregion Previews
