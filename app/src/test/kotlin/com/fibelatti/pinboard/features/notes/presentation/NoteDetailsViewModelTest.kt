package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockNoteId
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNote
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.shouldNeverReceiveValues
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class NoteDetailsViewModelTest : BaseViewModelTest() {

    private val mockNotesRepository = mockk<NotesRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)

    private val noteDetailsViewModel = NoteDetailsViewModel(
        mockNotesRepository,
        mockAppStateRepository
    )

    @Test
    fun `WHEN getNote fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        coEvery { mockNotesRepository.getNote(any()) } returns Failure(error)

        // WHEN
        noteDetailsViewModel.getNoteDetails(mockNoteId)

        // THEN
        noteDetailsViewModel.error.currentValueShouldBe(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN getAllNotes succeeds THEN AppStateRepository should run SetNotes`() {
        // GIVEN
        val mockNote = mockk<Note>()
        coEvery { mockNotesRepository.getNote(mockNoteId) } returns Success(mockNote)

        // WHEN
        noteDetailsViewModel.getNoteDetails(mockNoteId)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetNote(mockNote)) }
        noteDetailsViewModel.error.shouldNeverReceiveValues()
    }
}
