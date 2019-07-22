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
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetNotes
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never

internal class NoteListViewModelTest : BaseViewModelTest() {

    private val mockNotesRepository = mock<NotesRepository>()
    private val mockAppStateRepository = mock<AppStateRepository>()

    private val noteListViewModel = NoteListViewModel(
        mockNotesRepository,
        mockAppStateRepository
    )

    @Test
    fun `WHEN getAllNotes fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        givenSuspend { mockNotesRepository.getAllNotes() }
            .willReturn(Failure(error))

        // WHEN
        noteListViewModel.getAllNotes()

        // THEN
        noteListViewModel.error.currentValueShouldBe(error)
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `WHEN getAllNotes succeeds THEN AppStateRepository should run SetNotes`() {
        // GIVEN
        val mockNotes = mock<List<Note>>()
        givenSuspend { mockNotesRepository.getAllNotes() }
            .willReturn(Success(mockNotes))

        // WHEN
        noteListViewModel.getAllNotes()

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(SetNotes(mockNotes)) }
        noteListViewModel.error.shouldNeverReceiveValues()
    }
}
