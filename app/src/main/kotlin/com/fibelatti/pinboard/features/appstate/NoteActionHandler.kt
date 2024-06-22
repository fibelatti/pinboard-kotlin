package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import org.koin.core.annotation.Factory

@Factory
class NoteActionHandler(
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
        val body = { noteListContent: NoteListContent ->
            noteListContent.copy(
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }

        return currentContent
            .reduce(body)
            .reduce<NoteDetailContent> { noteDetailContent ->
                noteDetailContent.copy(
                    previousContent = body(noteDetailContent.previousContent),
                )
            }
    }

    private fun setNotes(action: SetNotes, currentContent: Content): Content {
        val body = { noteListContent: NoteListContent ->
            noteListContent.copy(
                notes = action.notes,
                shouldLoad = false,
            )
        }

        return currentContent
            .reduce(body)
            .reduce<NoteDetailContent> { noteDetailContent ->
                noteDetailContent.copy(
                    previousContent = body(noteDetailContent.previousContent),
                )
            }
    }

    private fun setNote(action: SetNote, currentContent: Content): Content {
        return currentContent.reduce<NoteDetailContent> { noteDetailContent ->
            noteDetailContent.copy(note = Either.Right(action.note))
        }
    }
}
