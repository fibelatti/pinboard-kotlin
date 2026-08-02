package com.fibelatti.pinboard.features.offline.presentation

import android.text.format.Formatter
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.ui.components.rememberAppSheetState
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

        val localAppMessages = LocalAppMessages.current

        val quickActionsSheetState = rememberAppSheetState()

        SideEffect(screenState.deleted) {
            if (screenState.deleted) {
                localAppMessages.show(R.string.offline_copies_deleted)
                offlineCopiesViewModel.userNotified()
            }
        }

        if (content.shouldLoad) {
            LoadingContent()
        } else {
            OfflineCopiesContent(
                offlineCopies = content.offlineCopies,
                onOfflineCopyClick = { offlineCopy ->
                    offlineCopiesViewModel.runAction(ViewOfflineCopy(offlineCopy))
                },
                onOfflineCopyLongClick = { offlineCopy ->
                    quickActionsSheetState.showBottomSheet(data = offlineCopy)
                },
                sidePanelVisible = appState.sidePanelVisible,
            )
        }

        val localContext = LocalContext.current

        OfflineCopyQuickActionsBottomSheet(
            sheetState = quickActionsSheetState,
            onDelete = { offlineCopy ->
                showOfflineCopyDeleteConfirmationDialog(context = localContext) {
                    offlineCopiesViewModel.delete(offlineCopy)
                }
            },
        )
    }
}

@Composable
private fun OfflineCopiesContent(
    offlineCopies: List<OfflineCopy>,
    onOfflineCopyClick: (OfflineCopy) -> Unit,
    onOfflineCopyLongClick: (OfflineCopy) -> Unit,
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

    val listInsets = WindowInsets.safeDrawing
        .only(if (sidePanelVisible) WindowInsetsSides.Start else WindowInsetsSides.Horizontal)
        .add(WindowInsets(top = 8.dp, bottom = 100.dp))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listInsets.asPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(offlineCopies, key = { "${it.appMode}-${it.bookmarkId}" }) { offlineCopy ->
            OfflineCopyListItem(
                offlineCopy = offlineCopy,
                onClick = { onOfflineCopyClick(offlineCopy) },
                onLongClick = { onOfflineCopyLongClick(offlineCopy) },
            )
        }
    }
}

@Composable
private fun OfflineCopyListItem(
    offlineCopy: OfflineCopy,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val localContext = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val host: String = remember(offlineCopy.url) {
        runCatching { URI(offlineCopy.url).host }.getOrNull().orEmpty()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterVertically),
        ) {
            Text(
                text = offlineCopy.title,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                text = listOfNotNull(
                    host.takeIf(String::isNotBlank),
                    Formatter.formatFileSize(localContext, offlineCopy.sizeBytes),
                ).joinToString(separator = " · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.MiddleEllipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
