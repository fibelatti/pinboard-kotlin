package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import javax.inject.Inject

class NoteActionHandler @Inject constructor(
    private val connectivityInfoProvider: ConnectivityInfoProvider
) : ActionHandler<NoteAction>() {

    override fun runAction(action: NoteAction, currentContent: Content): Content {
        return when (action) {
            is RefreshNotes -> refresh(currentContent)
            is SetNotes -> setNotes(action, currentContent)
            is SetNote -> setNote(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<NoteListContent>(currentContent) {
            it.copy(
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected()
            )
        }
    }

    private fun setNotes(action: SetNotes, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<NoteListContent>(currentContent) {
            it.copy(
                notes = action.notes,
                shouldLoad = false
            )
        }
    }

    private fun setNote(action: SetNote, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<NoteDetailContent>(currentContent) {
            it.copy(note = Either.right(action.note))
        }
    }
}
