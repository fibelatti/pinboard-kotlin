package com.fibelatti.bookmarking.features.notes.data

import com.fibelatti.bookmarking.MockDataProvider.MOCK_NOTE_ID
import com.fibelatti.bookmarking.features.notes.data.model.NoteDto
import com.fibelatti.bookmarking.features.notes.data.model.NoteDtoMapper
import com.fibelatti.bookmarking.features.notes.data.model.NoteListDto
import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class NotesDataSourceTest {

    private val mockApi = mockk<NotesApi>()
    private val mockNoteDtoMapper = mockk<NoteDtoMapper>()

    private val dataSource = NotesDataSource(mockApi, mockNoteDtoMapper)

    @Nested
    inner class GetAllNotesTests {

        @Test
        fun `GIVEN api returns an error WHEN getAllNotes is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getAllNotes() } throws Exception()

            // WHEN
            val result = dataSource.getAllNotes()

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN api returns successfully WHEN getAllNotes is called THEN Success is returned`() = runTest {
            // GIVEN
            val mockNoteListDto = mockk<NoteListDto>()
            val mockNotesDto = mockk<List<NoteDto>>()
            val mockNotes = mockk<List<Note>>()

            every { mockNoteListDto.notes } returns mockNotesDto
            coEvery { mockApi.getAllNotes() } returns mockNoteListDto
            every { mockNoteDtoMapper.mapList(mockNotesDto) } returns mockNotes

            // WHEN
            val result = dataSource.getAllNotes()

            // THEN
            assertThat(result.getOrNull()).isEqualTo(mockNotes)
        }
    }

    @Nested
    inner class GetNoteTests {

        @Test
        fun `GIVEN api returns an error WHEN getNote is called THEN Failure is returned`() = runTest {
            // GIVEN
            coEvery { mockApi.getNote(MOCK_NOTE_ID) } throws Exception()

            // WHEN
            val result = dataSource.getNote(MOCK_NOTE_ID)

            // THEN
            assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        }

        @Test
        fun `GIVEN api returns successfully WHEN getNote is called THEN Success is returned`() = runTest {
            // GIVEN
            val mockNoteDto = mockk<NoteDto>()
            val mockNote = mockk<Note>()

            coEvery { mockApi.getNote(MOCK_NOTE_ID) } returns mockNoteDto
            every { mockNoteDtoMapper.map(mockNoteDto) } returns mockNote

            // WHEN
            val result = dataSource.getNote(MOCK_NOTE_ID)

            // THEN
            assertThat(result.getOrNull()).isEqualTo(mockNote)
        }
    }
}
