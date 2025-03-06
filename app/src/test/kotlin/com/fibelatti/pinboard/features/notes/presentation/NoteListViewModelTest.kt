package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.SetNotes
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class NoteListViewModelTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }

    private val mockNotesRepository = mockk<NotesRepository>()
    private val mockDateFormatter = mockk<DateFormatter>()

    private val noteListViewModel = NoteListViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        notesRepository = mockNotesRepository,
        dateFormatter = mockDateFormatter,
    )

    @Test
    fun `WHEN NoteListContent is emitted AND getAllNotes fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockNotesRepository.getAllNotes() } returns Failure(error)

        // WHEN
        appStateFlow.value = createAppState(
            content = NoteListContent(
                notes = emptyList(),
                shouldLoad = true,
                previousContent = mockk(),
            ),
        )

        // THEN
        assertThat(noteListViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN NoteListContent is emitted AND getAllNotes succeeds THEN AppStateRepository should run SetNotes`() =
        runTest {
            // GIVEN
            val mockNotes = mockk<List<Note>>()
            coEvery { mockNotesRepository.getAllNotes() } returns Success(mockNotes)

            // WHEN
            appStateFlow.value = createAppState(
                content = NoteListContent(
                    notes = emptyList(),
                    shouldLoad = true,
                    previousContent = mockk(),
                ),
            )

            // THEN
            assertThat(noteListViewModel.error.first()).isNull()
            coVerify { mockAppStateRepository.runAction(SetNotes(mockNotes)) }
        }
}
