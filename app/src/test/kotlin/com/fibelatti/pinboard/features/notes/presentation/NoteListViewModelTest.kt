package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNotes
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.shouldNeverReceiveValues
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class NoteListViewModelTest : BaseViewModelTest() {

    private val mockNotesRepository = mockk<NotesRepository>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val mockDateFormatter = mockk<DateFormatter>()

    private val noteListViewModel = NoteListViewModel(
        mockNotesRepository,
        mockAppStateRepository,
        mockDateFormatter
    )

    @Test
    fun `WHEN getAllNotes fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        coEvery { mockNotesRepository.getAllNotes() } returns Failure(error)

        // WHEN
        noteListViewModel.getAllNotes()

        // THEN
        noteListViewModel.error.currentValueShouldBe(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN getAllNotes succeeds THEN AppStateRepository should run SetNotes`() {
        // GIVEN
        val mockNotes = mockk<List<Note>>()
        coEvery { mockNotesRepository.getAllNotes() } returns Success(mockNotes)

        // WHEN
        noteListViewModel.getAllNotes()

        // THEN
        coVerify { mockAppStateRepository.runAction(SetNotes(mockNotes)) }
        noteListViewModel.error.shouldNeverReceiveValues()
    }
}
