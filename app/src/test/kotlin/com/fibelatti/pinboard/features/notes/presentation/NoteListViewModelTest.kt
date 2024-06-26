package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.features.appstate.AppStateRepository
import com.fibelatti.bookmarking.features.appstate.SetNotes
import com.fibelatti.bookmarking.features.notes.domain.NotesRepository
import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.isEmpty
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class NoteListViewModelTest : BaseViewModelTest() {

    private val mockNotesRepository = mockk<NotesRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockDateFormatter = mockk<DateFormatter>()

    private val noteListViewModel = NoteListViewModel(
        mockNotesRepository,
        mockAppStateRepository,
        mockDateFormatter,
    )

    @Test
    fun `WHEN getAllNotes fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockNotesRepository.getAllNotes() } returns Failure(error)

        // WHEN
        noteListViewModel.getAllNotes()

        // THEN
        assertThat(noteListViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN getAllNotes succeeds THEN AppStateRepository should run SetNotes`() = runTest {
        // GIVEN
        val mockNotes = mockk<List<Note>>()
        coEvery { mockNotesRepository.getAllNotes() } returns Success(mockNotes)

        // WHEN
        noteListViewModel.getAllNotes()

        // THEN
        coVerify { mockAppStateRepository.runAction(SetNotes(mockNotes)) }
        assertThat(noteListViewModel.error.isEmpty()).isTrue()
    }
}
