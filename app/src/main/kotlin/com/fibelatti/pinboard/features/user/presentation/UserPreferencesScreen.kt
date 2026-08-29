package com.fibelatti.pinboard.features.user.presentation

import android.Manifest
import android.content.Context
import android.os.Build
import android.text.format.Formatter
import android.view.KeyEvent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.composable.LocalAppCompatActivity
import com.fibelatti.pinboard.core.android.composable.LocalAppMessages
import com.fibelatti.pinboard.core.android.composable.RadioSelectionDialogBottomSheet
import com.fibelatti.pinboard.core.android.composable.SelectionDialogCustomizationBottomSheet
import com.fibelatti.pinboard.core.android.composable.SettingToggle
import com.fibelatti.pinboard.core.android.getWindowSizeClass
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Close
import com.fibelatti.pinboard.core.android.icons.Edit
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.fillWidthOfParent
import com.fibelatti.pinboard.core.extension.materialAlertDialogBuilder
import com.fibelatti.pinboard.features.notifications.isNotificationPermissionGranted
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.PostQuickActions
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagManager
import com.fibelatti.pinboard.features.tags.presentation.displayTitle
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.ListItem
import com.fibelatti.ui.components.SingleLineChipGroup
import com.fibelatti.ui.components.rememberAppSheetState
import com.fibelatti.ui.foundation.Shapes
import com.fibelatti.ui.preview.PreviewAll
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserPreferencesScreen(
    modifier: Modifier = Modifier,
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
) {
    val appState by userPreferencesViewModel.appState.collectAsStateWithLifecycle()

    val localActivity = LocalActivity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()
    val restartActivity: () -> Unit by rememberUpdatedState {
        scope.launch {
            delay(timeMillis = 300L) // Wait until the switch is done animating
            localActivity?.let(ActivityCompat::recreate)
        }
    }

    DisposableEffect(Unit) {
        onDispose { keyboardController?.hide() }
    }

    val currentAppMode by rememberUpdatedState(appState.appMode)

    val appPreferences: @Composable (Modifier) -> Unit = remember {
        movableContentOf { contentModifier ->
            AppPreferencesContent(
                appMode = currentAppMode,
                onDynamicColorChange = restartActivity,
                onDisableScreenshotsChange = restartActivity,
                modifier = contentModifier,
            )
        }
    }

    val bookmarkingPreferences: @Composable (Modifier) -> Unit = remember {
        movableContentOf { contentModifier ->
            BookmarkingPreferencesContent(
                appMode = currentAppMode,
                modifier = contentModifier,
            )
        }
    }

    val isAtLeastMediumWidth: Boolean = getWindowSizeClass()
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val containerModifier = modifier
        .background(color = ExtendedTheme.colors.backgroundNoOverlay)
        .fillMaxSize()
        .windowInsetsPadding(
            WindowInsets.safeDrawing
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        )
        .verticalScroll(rememberScrollState())
        .padding(top = 8.dp, bottom = 32.dp)

    if (!isAtLeastMediumWidth) {
        Column(modifier = containerModifier) {
            appPreferences(Modifier)
            bookmarkingPreferences(Modifier.padding(top = 32.dp))
        }
    } else {
        Row(modifier = containerModifier) {
            appPreferences(Modifier.weight(1f))
            bookmarkingPreferences(Modifier.weight(1f))
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
    val offlineCopiesSize by userPreferencesViewModel.offlineCopiesSize.collectAsStateWithLifecycle()

    AppPreferencesContent(
        appMode = appMode,
        userPreferences = userPreferences,
        offlineCopiesSize = offlineCopiesSize,
        onClearOfflineCopiesClick = userPreferencesViewModel::clearOfflineCopies,
        onAppearanceChange = { newAppearance ->
            userPreferencesViewModel.saveAppearance(newAppearance)

            val mode = when (newAppearance) {
                is Appearance.DarkTheme -> AppCompatDelegate.MODE_NIGHT_YES
                is Appearance.LightTheme -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            AppCompatDelegate.setDefaultNightMode(mode)
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
        onUseSplitNavChange = userPreferencesViewModel::saveUseSplitNav,
        onMarkAsReadOnOpenChange = userPreferencesViewModel::saveMarkAsReadOnOpen,
        onShowDescriptionInListsChange = userPreferencesViewModel::saveShowDescriptionInLists,
        onAlphabetizeTagsChange = userPreferencesViewModel::saveAlphabetizeTags,
        modifier = modifier,
    )
}

@Composable
private fun AppPreferencesContent(
    appMode: AppMode,
    userPreferences: UserPreferences,
    offlineCopiesSize: Long,
    onClearOfflineCopiesClick: () -> Unit,
    onAppearanceChange: (Appearance) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDisableScreenshotsChange: (Boolean) -> Unit,
    onDateFormatChange: (PreferredDateFormat, Boolean) -> Unit,
    onPeriodicSyncChange: (PeriodicSync) -> Unit,
    onHiddenOptionsChange: (Set<String>) -> Unit,
    onPreferredViewChange: (PreferredDetailsView) -> Unit,
    onUseSplitNavChange: (Boolean) -> Unit,
    onMarkAsReadOnOpenChange: (Boolean) -> Unit,
    onShowDescriptionInListsChange: (Boolean) -> Unit,
    onAlphabetizeTagsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(id = R.string.user_preferences_section_app),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        val isDynamicColorSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        SettingItem(
            title = stringResource(id = R.string.user_preferences_appearance),
            shape = if (isDynamicColorSupported) Shapes.TopShape else Shapes.StandaloneShape,
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
                onOptionSelect = onAppearanceChange,
            )
        }

        if (isDynamicColorSupported) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_dynamic_colors),
                description = stringResource(id = R.string.user_preferences_dynamic_colors_caveat),
                checked = userPreferences.applyDynamicColors,
                onCheckedChange = onDynamicColorChange,
                shape = Shapes.BottomShape,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem(
            title = stringResource(id = R.string.user_preferences_date_format),
            shape = Shapes.TopShape,
        ) {
            PreferenceSelectionButton(
                currentSelection = userPreferences.preferredDateFormat,
                buttonText = { option: PreferredDateFormat ->
                    when (option) {
                        is PreferredDateFormat.DayMonthYearWithTime -> {
                            R.string.user_preferences_date_format_day_first
                        }

                        is PreferredDateFormat.MonthDayYearWithTime -> {
                            R.string.user_preferences_date_format_month_first
                        }

                        is PreferredDateFormat.ShortYearMonthDayWithTime -> {
                            R.string.user_preferences_date_format_short_year_first
                        }

                        is PreferredDateFormat.YearMonthDayWithTime -> {
                            R.string.user_preferences_date_format_year_first
                        }

                        is PreferredDateFormat.NoDate -> {
                            R.string.user_preferences_date_format_no_date
                        }
                    }
                },
                title = R.string.user_preferences_date_format,
                options = {
                    listOf(
                        PreferredDateFormat.DayMonthYearWithTime(),
                        PreferredDateFormat.MonthDayYearWithTime(),
                        PreferredDateFormat.ShortYearMonthDayWithTime(),
                        PreferredDateFormat.YearMonthDayWithTime(),
                        PreferredDateFormat.NoDate,
                    )
                },
                onOptionSelect = { newSelection ->
                    onDateFormatChange(newSelection, userPreferences.preferredDateFormat.includeTime)
                },
                footer = {
                    SettingToggle(
                        title = stringResource(R.string.user_preferences_date_format_include_time),
                        checked = userPreferences.preferredDateFormat.includeTime,
                        onCheckedChange = { newValue ->
                            onDateFormatChange(userPreferences.preferredDateFormat, newValue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_description_visible_in_lists),
            description = stringResource(id = R.string.user_preferences_description_visible_in_lists_description),
            checked = userPreferences.showDescriptionInLists,
            onCheckedChange = onShowDescriptionInListsChange,
            shape = Shapes.MiddleShape,
        )

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_alphabetize_tags),
            description = stringResource(id = R.string.user_preferences_alphabetize_tags_description),
            checked = userPreferences.alphabetizeTags,
            onCheckedChange = onAlphabetizeTagsChange,
            shape = Shapes.BottomShape,
        )

        Spacer(modifier = Modifier.height(12.dp))

        val markAsReadOnOpen: Boolean = when (val pdv = userPreferences.preferredDetailsView) {
            is PreferredDetailsView.ExternalBrowser -> pdv.markAsReadOnOpen
            is PreferredDetailsView.InAppBrowser -> pdv.markAsReadOnOpen
            is PreferredDetailsView.Edit -> false
        }
        val isPreferredDetailsViewEdit: Boolean = userPreferences.preferredDetailsView is PreferredDetailsView.Edit

        SettingItem(
            title = stringResource(id = R.string.user_preferences_preferred_details_view),
            shape = Shapes.TopShape,
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
                onOptionSelect = onPreferredViewChange,
            )

            Text(
                text = stringResource(id = R.string.user_preferences_preferred_details_view_caveat),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingToggle(
            title = stringResource(R.string.user_preferences_use_split_nav),
            description = stringResource(R.string.user_preferences_use_split_nav_description),
            checked = userPreferences.useSplitNav,
            onCheckedChange = onUseSplitNavChange,
            shape = if (isPreferredDetailsViewEdit) Shapes.BottomShape else Shapes.MiddleShape,
        )

        AnimatedVisibility(
            visible = !isPreferredDetailsViewEdit,
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
                shape = Shapes.BottomShape,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem(
            title = stringResource(R.string.user_preferences_offline_copies),
        ) {
            val localContext = LocalContext.current

            Text(
                text = stringResource(
                    R.string.user_preferences_offline_copies_description,
                    Formatter.formatFileSize(localContext, offlineCopiesSize),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PreferenceButton(
                buttonText = stringResource(R.string.user_preferences_offline_copies_clear),
                onClick = {
                    showClearOfflineCopiesConfirmationDialog(
                        context = localContext,
                        onConfirm = onClearOfflineCopiesClick,
                    )
                },
                enabled = offlineCopiesSize > 0,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingItem(
            title = stringResource(R.string.user_preferences_bookmark_quick_options),
        ) {
            Text(
                text = stringResource(R.string.user_preferences_bookmark_quick_actions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val bookmarkQuickActionCustomizationSheetState = rememberAppSheetState()
            val localResources = LocalResources.current
            val quickActionOptions = remember(appMode) {
                val samplePost = Post.EMPTY.copy(
                    description = "sample_description",
                    tags = listOf(Tag(name = "sample_tags")),
                )
                PostQuickActions.allOptions(samplePost, appMode = appMode).associateWith { option ->
                    option.serializedName in userPreferences.hiddenPostQuickOptions
                }
            }

            PreferenceButton(
                buttonText = stringResource(R.string.user_preferences_customize),
                onClick = bookmarkQuickActionCustomizationSheetState::showBottomSheet,
            )

            SelectionDialogCustomizationBottomSheet(
                sheetState = bookmarkQuickActionCustomizationSheetState,
                title = stringResource(R.string.user_preferences_bookmark_quick_options),
                options = quickActionOptions,
                optionName = { option -> localResources.getString(option.title) },
                optionIcon = PostQuickActions::icon,
                onConfirm = { options ->
                    val hiddenOptions = options.filterValues { hidden -> hidden }.keys
                        .map { it.serializedName }
                        .toSet()

                    onHiddenOptionsChange(hiddenOptions)
                },
            )
        }

        if (AppMode.NO_API != appMode) {
            Spacer(modifier = Modifier.height(12.dp))

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
                    onOptionSelect = onPeriodicSyncChange,
                )

                Text(
                    text = stringResource(id = R.string.user_preferences_periodic_sync_caveat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_disable_screenshots),
            description = stringResource(id = R.string.user_preferences_disable_screenshots_caveat),
            checked = userPreferences.disableScreenshots,
            onCheckedChange = onDisableScreenshotsChange,
        )
    }
}

private fun showClearOfflineCopiesConfirmationDialog(context: Context, onConfirm: () -> Unit) {
    context.materialAlertDialogBuilder().apply {
        setTitle(R.string.user_preferences_offline_copies_clear)
        setMessage(R.string.user_preferences_offline_copies_clear_confirmation)
        setPositiveButton(R.string.hint_yes) { _, _ -> onConfirm() }
        setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
    }.applySecureFlag().show()
}

@Composable
private fun BookmarkingPreferencesContent(
    appMode: AppMode,
    modifier: Modifier = Modifier,
    userPreferencesViewModel: UserPreferencesViewModel = hiltViewModel(),
) {
    val userPreferences by userPreferencesViewModel.currentPreferences.collectAsStateWithLifecycle()
    val tagState by userPreferencesViewModel.tagManagerState.collectAsStateWithLifecycle(TagManagerState())

    val localAppCompatActivity = LocalAppCompatActivity.current
    val localAppMessages = LocalAppMessages.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted: Boolean ->
            if (!granted) {
                localAppMessages.show(
                    messageRes = R.string.user_preferences_use_background_share_receiver_missing_permission,
                )
            }
        },
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BookmarkingPreferencesContent(
            appMode = appMode,
            userPreferences = userPreferences,
            onEditAfterSharingChange = userPreferencesViewModel::saveEditAfterSharing,
            onUseBackgroundShareReceiverChange = { newValue: Boolean ->
                userPreferencesViewModel.saveUseBackgroundShareReceiver(newValue)

                val permissionNotGranted: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !localAppCompatActivity.isNotificationPermissionGranted()
                if (newValue && permissionNotGranted) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
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
            shape = Shapes.BottomShape,
        ) {
            Text(
                text = stringResource(id = R.string.user_preferences_default_tags_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TagManager(
                searchTagInput = tagState.currentQuery,
                onSearchTagInputChange = userPreferencesViewModel::setTagSearchQuery,
                onAddTagClick = userPreferencesViewModel::addTag,
                suggestedTags = tagState.suggestedTags,
                onSuggestedTagClick = userPreferencesViewModel::addTag,
                currentTagsTitle = tagState.displayTitle,
                currentTags = tagState.tags,
                onRemoveCurrentTagClick = userPreferencesViewModel::removeTag,
                modifier = Modifier.fillWidthOfParent(parentPaddingStart = 16.dp, parentPaddingEnd = 16.dp),
            )
        }
    }
}

@Composable
private fun BookmarkingPreferencesContent(
    appMode: AppMode,
    userPreferences: UserPreferences,
    onEditAfterSharingChange: (EditAfterSharing) -> Unit,
    onUseBackgroundShareReceiverChange: (Boolean) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(id = R.string.user_preferences_section_bookmarking),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingItem(
            title = stringResource(id = R.string.user_preferences_edit_after_sharing_title),
            shape = Shapes.TopShape,
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
                onOptionSelect = onEditAfterSharingChange,
            )

            Text(
                text = stringResource(id = R.string.user_preferences_edit_after_sharing_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_use_background_share_receiver),
            description = stringResource(id = R.string.user_preferences_use_background_share_receiver_description),
            checked = userPreferences.useBackgroundShareReceiver,
            onCheckedChange = onUseBackgroundShareReceiverChange,
            shape = Shapes.BottomShape,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_follow_redirects),
            description = stringResource(id = R.string.user_preferences_follow_redirects_description),
            checked = userPreferences.followRedirects,
            onCheckedChange = onFollowRedirectsChange,
            shape = Shapes.TopShape,
        )

        RemoveUrlParametersSetting(
            removeUtmParameters = userPreferences.removeUtmParameters,
            onRemoveUtmParametersChange = onRemoveUtmParametersChange,
            removedParameters = userPreferences.removedUrlParameters,
            onRemovedParametersChange = onRemovedUrlParametersChange,
        )

        val autoFillDescription: Boolean = userPreferences.autoFillDescription

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_description_auto_fill),
            description = stringResource(id = R.string.user_preferences_description_auto_fill_description),
            checked = autoFillDescription,
            onCheckedChange = onAutoFillDescriptionChange,
            shape = if (autoFillDescription) Shapes.MiddleShape else Shapes.BottomShape,
        )

        AnimatedVisibility(
            visible = autoFillDescription,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_use_blockquote),
                description = stringResource(id = R.string.user_preferences_use_blockquote_description),
                checked = userPreferences.useBlockquote,
                onCheckedChange = onUseBlockquoteChange,
                shape = Shapes.BottomShape,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingToggle(
            title = stringResource(id = R.string.user_preferences_default_read_later_label),
            description = stringResource(id = R.string.user_preferences_default_read_later_description),
            checked = userPreferences.defaultReadLater,
            onCheckedChange = onReadLaterByDefaultChange,
            shape = Shapes.TopShape,
        )

        if (AppMode.NO_API != appMode) {
            SettingToggle(
                title = stringResource(id = R.string.user_preferences_default_private_label),
                description = stringResource(id = R.string.user_preferences_default_private_description),
                checked = userPreferences.defaultPrivate,
                onCheckedChange = onPrivateByDefaultChange,
                shape = Shapes.MiddleShape,
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingToggle(
            title = stringResource(R.string.user_preferences_remove_utm_parameters),
            description = stringResource(R.string.user_preferences_remove_utm_parameters_description),
            checked = removeUtmParameters,
            onCheckedChange = onRemoveUtmParametersChange,
            shape = Shapes.MiddleShape,
        )

        SettingItem(
            title = stringResource(R.string.user_preferences_remove_url_parameters),
            modifier = Modifier.animateContentSize(),
            shape = Shapes.MiddleShape,
        ) {
            Text(
                text = stringResource(R.string.user_preferences_remove_url_parameters_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            val textFieldState = rememberTextFieldState()

            val submitValueAction = {
                if (textFieldState.text.isNotBlank()) {
                    onRemovedParametersChange(removedParameters + textFieldState.text.toString())
                }
                textFieldState.setTextAndPlaceCursorAtEnd("")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    state = textFieldState,
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
                    onKeyboardAction = KeyboardActionHandler { submitValueAction() },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    shape = Shapes.StandaloneShape,
                )

                FilledTonalButton(
                    onClick = submitValueAction,
                    shapes = ExtendedTheme.defaultButtonShapes,
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Text(text = stringResource(R.string.hint_add))
                }
            }

            val closeIcon = rememberVectorPainter(AppIcons.Close)
            SingleLineChipGroup(
                items = remember(removedParameters) {
                    removedParameters.map { parameter -> ChipGroup.Item(text = parameter, icon = closeIcon) }
                },
                onItemClick = { item -> onRemovedParametersChange(removedParameters - item.text) },
                modifier = Modifier
                    .fillWidthOfParent(parentPaddingStart = 12.dp, parentPaddingEnd = 12.dp)
                    .fillMaxWidth(),
                itemTextStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
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
    shape: Shape = Shapes.StandaloneShape,
    body: @Composable ColumnScope.() -> Unit,
) {
    androidx.compose.material3.ListItem(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListItem.MinHeight)
            .clip(shape),
        supportingContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = body,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun PreferenceButton(
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalButton(
        onClick = onClick,
        shapes = ExtendedTheme.defaultButtonShapes,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        enabled = enabled,
    ) {
        Text(
            text = buttonText,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(4.dp))

        Icon(
            imageVector = AppIcons.Edit,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun <T : Any> PreferenceSelectionButton(
    currentSelection: T,
    buttonText: (T) -> Int,
    @StringRes title: Int,
    options: () -> List<T>,
    onOptionSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit = {},
) {
    val localResources = LocalResources.current
    val sheetState = rememberAppSheetState()

    PreferenceButton(
        buttonText = stringResource(buttonText(currentSelection)),
        onClick = sheetState::showBottomSheet,
        modifier = modifier,
    )

    RadioSelectionDialogBottomSheet(
        sheetState = sheetState,
        title = stringResource(title),
        options = options(),
        optionName = { option -> localResources.getString(buttonText(option)) },
        currentSelection = currentSelection,
        onOptionSelect = onOptionSelect,
        footer = footer,
    )
}
// endregion Components

// region Previews
@Composable
@PreviewAll
private fun AppPreferencesContentPreview(
    @PreviewParameter(UserPreferencesProvider::class) userPreferences: UserPreferences,
) {
    ExtendedTheme {
        AppPreferencesContent(
            appMode = AppMode.PINBOARD,
            userPreferences = userPreferences,
            offlineCopiesSize = 0,
            onClearOfflineCopiesClick = {},
            onAppearanceChange = {},
            onDynamicColorChange = {},
            onDisableScreenshotsChange = {},
            onDateFormatChange = { _, _ -> },
            onHiddenOptionsChange = {},
            onPeriodicSyncChange = {},
            onPreferredViewChange = {},
            onUseSplitNavChange = {},
            onMarkAsReadOnOpenChange = {},
            onShowDescriptionInListsChange = {},
            onAlphabetizeTagsChange = {},
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@Composable
@PreviewAll
private fun BookmarkingPreferencesContentPreview(
    @PreviewParameter(UserPreferencesProvider::class) userPreferences: UserPreferences,
) {
    ExtendedTheme {
        BookmarkingPreferencesContent(
            appMode = AppMode.PINBOARD,
            userPreferences = userPreferences,
            onEditAfterSharingChange = {},
            onUseBackgroundShareReceiverChange = {},
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
