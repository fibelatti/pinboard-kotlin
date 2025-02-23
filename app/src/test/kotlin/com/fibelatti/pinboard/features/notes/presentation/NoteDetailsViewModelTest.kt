package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_NOTE_ID
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNote
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class NoteDetailsViewModelTest : BaseViewModelTest() {

    private val mockNotesRepository = mockk<NotesRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)

    private val noteDetailsViewModel = NoteDetailsViewModel(
        mockNotesRepository,
        mockAppStateRepository,
    )

    @Test
    fun `WHEN getNote fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockNotesRepository.getNote(any()) } returns Failure(error)

        // WHEN
        noteDetailsViewModel.getNoteDetails(SAMPLE_NOTE_ID)

        // THEN
        assertThat(noteDetailsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN getAllNotes succeeds THEN AppStateRepository should run SetNotes`() = runTest {
        // GIVEN
        val mockNote = mockk<Note>()
        coEvery { mockNotesRepository.getNote(SAMPLE_NOTE_ID) } returns Success(mockNote)

        // WHEN
        noteDetailsViewModel.getNoteDetails(SAMPLE_NOTE_ID)

        // THEN
        coVerify { mockAppStateRepository.runAction(SetNote(mockNote)) }
        assertThat(noteDetailsViewModel.error.isEmpty()).isTrue()
    }
}
