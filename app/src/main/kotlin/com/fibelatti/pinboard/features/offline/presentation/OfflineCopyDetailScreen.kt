package com.fibelatti.pinboard.features.offline.presentation

import android.content.Context
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.applySecureFlag
import com.fibelatti.pinboard.core.extension.materialAlertDialogBuilder
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.OfflineCopyDetailContent
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainViewModel
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun OfflineCopyDetailScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel(),
    offlineCopiesViewModel: OfflineCopiesViewModel = hiltViewModel(),
) {
    Surface(
        modifier = modifier,
        color = ExtendedTheme.colors.backgroundNoOverlay,
    ) {
        val appState: AppState by mainViewModel.appState.collectAsStateWithLifecycle()
        val content: OfflineCopyDetailContent = appState.content.find() ?: return@Surface
        val offlineCopy: OfflineCopy = content.offlineCopy

        val localContext = LocalContext.current
        val localUriHandler = LocalUriHandler.current
        val localLifecycle = LocalLifecycleOwner.current.lifecycle
        val localOnBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        LaunchedEffect(Unit) {
            mainViewModel.menuItemClicks(contentType = OfflineCopyDetailContent::class)
                .onEach { (menuItem, data) ->
                    if (data !is OfflineCopy) return@onEach

                    when (menuItem) {
                        is MainState.MenuItemComponent.OpenInBrowser -> {
                            localUriHandler.openUri(data.url)
                        }

                        is MainState.MenuItemComponent.RemoveOfflineCopy -> {
                            showOfflineCopyDeleteConfirmationDialog(context = localContext) {
                                offlineCopiesViewModel.delete(offlineCopy = data, fromDetails = true)
                            }
                        }

                        is MainState.MenuItemComponent.CloseSidePanel -> {
                            localOnBackPressedDispatcher?.onBackPressed()
                        }

                        else -> Unit
                    }
                }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)

            mainViewModel.fabClicks(contentType = OfflineCopyDetailContent::class)
                .onEach { data -> (data as? OfflineCopy)?.let { localUriHandler.openUri(it.url) } }
                .flowWithLifecycle(localLifecycle)
                .launchIn(this)
        }

        val file = remember(offlineCopy) { offlineCopiesViewModel.fileFor(offlineCopy) }

        OfflineCopyWebView(
            offlineCopy = offlineCopy,
            file = file,
            onExternalLinkClick = localUriHandler::openUri,
            onScrollDirectionChange = mainViewModel::setCurrentScrollDirection,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Deleting a copy is not undoable — the file is gone and the page has to be fetched again — so every
 * entry point confirms first.
 */
internal fun showOfflineCopyDeleteConfirmationDialog(context: Context, onConfirm: () -> Unit) {
    context.materialAlertDialogBuilder().apply {
        setMessage(R.string.alert_confirm_deletion_offline_copy)
        setPositiveButton(R.string.hint_yes) { _, _ -> onConfirm() }
        setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
    }.applySecureFlag().show()
}

/**
 * The bulk variant of [showOfflineCopyDeleteConfirmationDialog], spelling out how many copies are
 * about to go since the selection isn't visible behind the dialog.
 */
internal fun showOfflineCopyBulkDeleteConfirmationDialog(
    context: Context,
    count: Int,
    onConfirm: () -> Unit,
) {
    context.materialAlertDialogBuilder().apply {
        setMessage(
            context.resources.getQuantityString(
                R.plurals.alert_confirm_deletion_offline_copies,
                count,
                count,
            ),
        )
        setPositiveButton(R.string.hint_yes) { _, _ -> onConfirm() }
        setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
    }.applySecureFlag().show()
}
