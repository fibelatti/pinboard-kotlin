package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import javax.inject.Inject

class NoteActionHandler @Inject constructor(
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : ActionHandler<NoteAction>() {

    override suspend fun runAction(action: NoteAction, currentContent: Content): Content {
        return when (action) {
            is RefreshNotes -> refresh(currentContent)
            is SetNotes -> setNotes(action, currentContent)
            is SetNote -> setNote(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return currentContent.reduce<NoteListContent> { noteListContent ->
            noteListContent.copy(
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun setNotes(action: SetNotes, currentContent: Content): Content {
        return currentContent.reduce<NoteListContent> { noteListContent ->
            noteListContent.copy(
                notes = action.notes,
                shouldLoad = false,
            )
        }
    }

    private fun setNote(action: SetNote, currentContent: Content): Content {
        return currentContent.reduce<NoteDetailContent> { noteListContent ->
            noteListContent.copy(note = Either.Right(action.note))
        }
    }
}
