package com.fibelatti.pinboard.features.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.export.ExportBookmarksUseCase
import com.fibelatti.pinboard.features.export.MoveFileToUriUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltViewModel
class NavigationMenuViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val exportBookmarksUseCase: ExportBookmarksUseCase,
    private val moveFileToUriUseCase: MoveFileToUriUseCase,
) : BaseViewModel(scope, appStateRepository) {

    var state: State by mutableStateOf(State())
        private set

    fun createBackup() {
        scope.launch {
            state = state.copy(isProcessing = true)
            val file: File? = exportBookmarksUseCase()

            state = state.copy(
                isProcessing = false,
                preparedFile = file,
                messages = state.messages + listOfNotNull(State.Message.EXPORT_FAILURE.takeIf { file == null }),
            )
        }
    }

    fun exportFile(destinationUri: Uri?) {
        val preparedFile: File? = state.preparedFile

        if (preparedFile == null || destinationUri == null) {
            state = state.copy(
                preparedFile = null,
                messages = state.messages + State.Message.EXPORT_FAILURE,
            )
            return
        }

        scope.launch {
            val result: Long? = moveFileToUriUseCase(
                params = MoveFileToUriUseCase.Params(sourceFile = preparedFile, destinationUri = destinationUri),
            )

            val message: State.Message = if (result != null && result > 0) {
                State.Message.EXPORT_SUCCESS
            } else {
                State.Message.EXPORT_FAILURE
            }

            state = state.copy(
                preparedFile = null,
                messages = state.messages + message,
            )
        }
    }

    fun messageHandled(message: State.Message) {
        state = state.copy(messages = state.messages - message)
    }

    data class State(
        val isProcessing: Boolean = false,
        val preparedFile: File? = null,
        val messages: List<Message> = emptyList(),
    ) {

        enum class Message {
            EXPORT_SUCCESS,
            EXPORT_FAILURE,
        }
    }
}
