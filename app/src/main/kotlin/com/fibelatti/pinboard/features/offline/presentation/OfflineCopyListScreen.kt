package com.fibelatti.pinboard.features.offline.presentation

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.EmptyListContent
import com.fibelatti.pinboard.core.android.composable.LoadingContent
import com.fibelatti.pinboard.core.android.composable.LocalAppMessages
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Download
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.OfflineCopyListContent
import com.fibelatti.pinboard.features.appstate.ViewOfflineCopy
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.main.MainBottomAppBar
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.ui.components.rememberAppSheetState
import com.fibelatti.ui.foundation.Shapes
import com.fibelatti.ui.theme.ExtendedTheme
import java.net.URI

@Composable
fun OfflineCopyListScreen(
    modifier: Modifier = Modifier,
    offlineCopiesViewModel: OfflineCopiesViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState: AppState by offlineCopiesViewModel.appState.collectAsStateWithLifecycle()
        val content: OfflineCopyListContent = appState.content.find() ?: return@Surface

        val screenState by offlineCopiesViewModel.screenState.collectAsStateWithLifecycle()

        val localContext = LocalContext.current
        val localAppMessages = LocalAppMessages.current

        val quickActionsSheetState = rememberAppSheetState()

        val selectedKeys: SnapshotStateList<String> = rememberSaveable(
            saver = listSaver(
                save = { it.toList() },
                restore = { it.toMutableStateList() },
            ),
            init = ::mutableStateListOf,
        )

        val selectedCopies: List<OfflineCopy> = content.offlineCopies.filter { it.key in selectedKeys }
        val selectionActive: Boolean = selectedCopies.isNotEmpty()

        SideEffect(screenState.deletedCount) {
            if (screenState.deletedCount > 0) {
                localAppMessages.show(
                    messageRes = if (screenState.deletedCount == 1) {
                        R.string.offline_copies_deleted
                    } else {
                        R.string.offline_copies_bulk_deleted
                    },
                )
                offlineCopiesViewModel.userNotified()
            }
        }

        BackHandler(enabled = selectionActive) {
            selectedKeys.clear()
        }

        if (content.shouldLoad) {
            LoadingContent()
        } else {
            OfflineCopiesContent(
                offlineCopies = content.offlineCopies,
                selectedKeys = selectedKeys,
                selectedCount = selectedCopies.size,
                onOfflineCopyClick = { offlineCopy ->
                    if (selectionActive) {
                        if (!selectedKeys.remove(offlineCopy.key)) {
                            selectedKeys.add(offlineCopy.key)
                        }
                    } else {
                        offlineCopiesViewModel.runAction(ViewOfflineCopy(offlineCopy))
                    }
                },
                onOfflineCopyLongClick = { offlineCopy ->
                    if (!selectionActive) {
                        quickActionsSheetState.showBottomSheet(data = offlineCopy)
                    }
                },
                onCancelSelectionClick = selectedKeys::clear,
                onBulkDeleteClick = {
                    showOfflineCopyBulkDeleteConfirmationDialog(
                        context = localContext,
                        count = selectedCopies.size,
                    ) {
                        offlineCopiesViewModel.delete(selectedCopies)
                        selectedKeys.clear()
                    }
                },
                sidePanelVisible = appState.sidePanelVisible,
            )
        }

        OfflineCopyQuickActionsBottomSheet(
            sheetState = quickActionsSheetState,
            onDelete = { offlineCopy ->
                showOfflineCopyDeleteConfirmationDialog(context = localContext) {
                    offlineCopiesViewModel.delete(offlineCopy)
                }
            },
            onBulkDelete = { offlineCopy ->
                selectedKeys.add(offlineCopy.key)
            },
        )
    }
}

/**
 * Identifies a copy in the local selection. Selecting by key rather than by instance keeps the
 * selection intact when the app state hands back new [OfflineCopy] objects.
 */
private val OfflineCopy.key: String
    get() = "$appMode-$bookmarkId"

@Composable
private fun OfflineCopiesContent(
    offlineCopies: List<OfflineCopy>,
    selectedKeys: List<String>,
    selectedCount: Int,
    onOfflineCopyClick: (OfflineCopy) -> Unit,
    onOfflineCopyLongClick: (OfflineCopy) -> Unit,
    onCancelSelectionClick: () -> Unit,
    onBulkDeleteClick: () -> Unit,
    sidePanelVisible: Boolean,
) {
    if (offlineCopies.isEmpty()) {
        EmptyListContent(
            icon = AppIcons.Download,
            title = stringResource(id = R.string.offline_copies_empty_title),
            description = stringResource(id = R.string.offline_copies_empty_description),
        )
        return
    }

    val bottomInset: WindowInsets = WindowInsets.navigationBars
        .add(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Bottom)
        .add(WindowInsets(bottom = MainBottomAppBar.ContentClearance))

    val listInsets: WindowInsets = WindowInsets.safeDrawing
        .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
        .add(WindowInsets(top = 8.dp))
        .add(bottomInset)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listInsets.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(offlineCopies, key = { it.key }) { offlineCopy ->
                OfflineCopyListItem(
                    offlineCopy = offlineCopy,
                    isSelected = offlineCopy.key in selectedKeys,
                    onClick = { onOfflineCopyClick(offlineCopy) },
                    onLongClick = { onOfflineCopyLongClick(offlineCopy) },
                )
            }
        }

        AnimatedVisibility(
            visible = selectedCount > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            val colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                            .add(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    )
                    .padding(bottom = 16.dp),
                colors = colors,
                shape = Shapes.StandaloneShape,
            ) {
                TextButton(
                    onClick = onCancelSelectionClick,
                    shapes = ExtendedTheme.defaultButtonShapes,
                ) {
                    Text(
                        text = stringResource(id = R.string.hint_cancel),
                        color = colors.toolbarContentColor,
                    )
                }

                Button(
                    onClick = onBulkDeleteClick,
                    shapes = ExtendedTheme.defaultButtonShapes,
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.offline_copies_bulk_delete_action,
                            selectedCount,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineCopyListItem(
    offlineCopy: OfflineCopy,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val localContext = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val host: String = remember(offlineCopy.url) {
        runCatching { URI(offlineCopy.url).host }.getOrNull().orEmpty()
    }

    val scale: Float by animateFloatAsState(targetValue = if (isSelected) .95f else 1f)
    val containerColor: Color by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    )
    val contentColor: Color = contentColorFor(containerColor)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = 8.dp)
            .scale(scale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        shape = Shapes.StandaloneShape,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        ) {
            Text(
                text = offlineCopy.title,
                color = contentColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = listOfNotNull(
                    host.takeIf(String::isNotBlank),
                    Formatter.formatFileSize(localContext, offlineCopy.sizeBytes),
                ).joinToString(separator = " · "),
                color = contentColor,
                overflow = TextOverflow.MiddleEllipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
