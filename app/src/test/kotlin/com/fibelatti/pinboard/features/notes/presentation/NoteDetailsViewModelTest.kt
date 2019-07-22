package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockNoteId
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNote
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never

internal class NoteDetailsViewModelTest : BaseViewModelTest() {

    private val mockNotesRepository = mock<NotesRepository>()
    private val mockAppStateRepository = mock<AppStateRepository>()

    private val noteDetailsViewModel = NoteDetailsViewModel(
        mockNotesRepository,
        mockAppStateRepository
    )

    @Test
    fun `WHEN getNote fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        givenSuspend { mockNotesRepository.getNote(anyString()) }
            .willReturn(Failure(error))

        // WHEN
        noteDetailsViewModel.getNoteDetails(mockNoteId)

        // THEN
        noteDetailsViewModel.error.currentValueShouldBe(error)
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `WHEN getAllNotes succeeds THEN AppStateRepository should run SetNotes`() {
        // GIVEN
        val mockNote = mock<Note>()
        givenSuspend { mockNotesRepository.getNote(mockNoteId) }
            .willReturn(Success(mockNote))

        // WHEN
        noteDetailsViewModel.getNoteDetails(mockNoteId)

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(SetNote(mockNote)) }
        noteDetailsViewModel.error.shouldNeverReceiveValues()
    }
}
