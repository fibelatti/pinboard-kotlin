package com.fibelatti.pinboard.features.notes.presentation

import com.fibelatti.core.functional.Either
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_NOTE_ID
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.SetNote
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

internal class NoteDetailsViewModelTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }

    private val mockNotesRepository = mockk<NotesRepository>()

    private val noteDetailsViewModel = NoteDetailsViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        notesRepository = mockNotesRepository,
    )

    @Test
    fun `WHEN NoteDetailContent is emitted AND getNote fails THEN error should receive a value`() = runTest {
        // GIVEN
        val error = Exception()
        coEvery { mockNotesRepository.getNote(any()) } returns Failure(error)

        // WHEN
        appStateFlow.value = createAppState(
            content = NoteDetailContent(
                id = SAMPLE_NOTE_ID,
                note = Either.Left(true),
                previousContent = mockk(),
            ),
        )

        // THEN
        assertThat(noteDetailsViewModel.error.first()).isEqualTo(error)
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `WHEN NoteDetailContent is emitted AND getAllNotes succeeds THEN AppStateRepository should run SetNotes`() =
        runTest {
            // GIVEN
            val mockNote = mockk<Note>()
            coEvery { mockNotesRepository.getNote(SAMPLE_NOTE_ID) } returns Success(mockNote)

            // WHEN
            appStateFlow.value = createAppState(
                content = NoteDetailContent(
                    id = SAMPLE_NOTE_ID,
                    note = Either.Left(true),
                    previousContent = mockk(),
                ),
            )

            // THEN
            assertThat(noteDetailsViewModel.error.first()).isNull()
            coVerify { mockAppStateRepository.runAction(SetNote(mockNote)) }
        }
}
