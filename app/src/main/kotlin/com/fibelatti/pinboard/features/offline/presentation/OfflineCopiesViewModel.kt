package com.fibelatti.pinboard.features.offline.presentation

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.OfflineCopyListContent
import com.fibelatti.pinboard.features.appstate.SetOfflineCopies
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OfflineCopiesViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val offlineCopyRepository: OfflineCopyRepository,
) : BaseViewModel(scope, appStateRepository) {

    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    init {
        filteredContent<OfflineCopyListContent>()
            .filter { it.shouldLoad }
            .flatMapLatest { offlineCopyRepository.getOfflineCopies(appMode = appState.value.appMode) }
            .onEach { offlineCopies -> runAction(SetOfflineCopies(offlineCopies)) }
            .launchIn(scope)
    }

    fun fileFor(offlineCopy: OfflineCopy): File = offlineCopyRepository.fileFor(offlineCopy)

    fun delete(offlineCopy: OfflineCopy, fromDetails: Boolean = false) {
        scope.launch {
            offlineCopyRepository.delete(
                appMode = offlineCopy.appMode,
                bookmarkId = offlineCopy.bookmarkId,
            )
            _screenState.update { it.copy(deletedCount = 1) }
            if (fromDetails) {
                runDelayedAction(NavigateBack)
            }
        }
    }

    fun delete(offlineCopies: List<OfflineCopy>) {
        scope.launch {
            for (offlineCopy in offlineCopies) {
                offlineCopyRepository.delete(
                    appMode = offlineCopy.appMode,
                    bookmarkId = offlineCopy.bookmarkId,
                )
            }
            _screenState.update { it.copy(deletedCount = offlineCopies.size) }
        }
    }

    fun userNotified() {
        _screenState.update { it.copy(deletedCount = 0) }
    }

    data class ScreenState(
        val deletedCount: Int = 0,
    )
}
