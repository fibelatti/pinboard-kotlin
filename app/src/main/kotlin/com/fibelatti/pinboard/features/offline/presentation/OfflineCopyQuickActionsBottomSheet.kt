package com.fibelatti.pinboard.features.offline.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.SelectionDialogBottomSheet
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Browser
import com.fibelatti.pinboard.core.android.icons.Delete
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.ui.components.AppSheetState

@Composable
fun OfflineCopyQuickActionsBottomSheet(
    sheetState: AppSheetState,
    onDelete: (OfflineCopy) -> Unit,
    onBulkDelete: (OfflineCopy) -> Unit,
) {
    val offlineCopy: OfflineCopy = sheetState.bottomSheetData() ?: return
    val localResources = LocalResources.current
    val localUriHandler = LocalUriHandler.current

    SelectionDialogBottomSheet(
        sheetState = sheetState,
        title = stringResource(R.string.quick_actions_title),
        options = OfflineCopyQuickActions.allOptions(offlineCopy),
        optionName = { option -> localResources.getString(option.title) },
        optionIcon = OfflineCopyQuickActions::icon,
        onOptionSelect = { option ->
            when (option) {
                is OfflineCopyQuickActions.OpenOriginal -> localUriHandler.openUri(option.offlineCopy.url)
                is OfflineCopyQuickActions.Delete -> onDelete(option.offlineCopy)
                is OfflineCopyQuickActions.BulkDelete -> onBulkDelete(option.offlineCopy)
            }
        },
    )
}

private sealed class OfflineCopyQuickActions(
    @StringRes val title: Int,
    val icon: ImageVector,
) {

    abstract val offlineCopy: OfflineCopy

    data class OpenOriginal(
        override val offlineCopy: OfflineCopy,
    ) : OfflineCopyQuickActions(
        title = R.string.offline_copies_open_original,
        icon = AppIcons.Browser,
    )

    data class Delete(
        override val offlineCopy: OfflineCopy,
    ) : OfflineCopyQuickActions(
        title = R.string.offline_copies_delete,
        icon = AppIcons.Delete,
    )

    data class BulkDelete(
        override val offlineCopy: OfflineCopy,
    ) : OfflineCopyQuickActions(
        title = R.string.offline_copies_bulk_delete,
        icon = AppIcons.Delete,
    )

    companion object {

        fun allOptions(offlineCopy: OfflineCopy): List<OfflineCopyQuickActions> = listOf(
            OpenOriginal(offlineCopy),
            Delete(offlineCopy),
            BulkDelete(offlineCopy),
        )
    }
}
