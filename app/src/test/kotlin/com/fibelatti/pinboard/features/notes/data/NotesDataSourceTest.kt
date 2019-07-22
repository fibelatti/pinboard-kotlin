package com.fibelatti.pinboard.features.notes.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.mockNoteId
import com.fibelatti.pinboard.TestRateLimitRunner
import com.fibelatti.pinboard.features.notes.data.model.NoteDto
import com.fibelatti.pinboard.features.notes.data.model.NoteDtoMapper
import com.fibelatti.pinboard.features.notes.data.model.NoteListDto
import com.fibelatti.pinboard.features.notes.domain.model.Note
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

internal class NotesDataSourceTest {

    private val mockApi = mock<NotesApi>()
    private val mockNoteDtoMapper = mock<NoteDtoMapper>()
    private val mockRunner = TestRateLimitRunner()

    private val dataSource = NotesDataSource(mockApi, mockNoteDtoMapper, mockRunner)

    @Nested
    inner class GetAllNotesTests {

        @Test
        fun `GIVEN api returns an error WHEN getAllNotes is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getAllNotes() }
                .willAnswer { throw Exception() }

            // WHEN
            val result = callSuspend { dataSource.getAllNotes() }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN api returns successfully WHEN getAllNotes is called THEN Success is returned`() {
            // GIVEN
            val mockNoteListDto = mock<NoteListDto>()
            val mockNotesDto = mock<List<NoteDto>>()
            val mockNotes = mock<List<Note>>()

            given(mockNoteListDto.notes)
                .willReturn(mockNotesDto)
            givenSuspend { mockApi.getAllNotes() }
                .willReturn(mockNoteListDto)
            given(mockNoteDtoMapper.mapList(mockNotesDto))
                .willReturn(mockNotes)

            // WHEN
            val result = callSuspend { dataSource.getAllNotes() }

            // THEN
            result.getOrNull() shouldBe mockNotes
        }
    }

    @Nested
    inner class GetNoteTests {

        @Test
        fun `GIVEN api returns an error WHEN getNote is called THEN Failure is returned`() {
            // GIVEN
            givenSuspend { mockApi.getNote(mockNoteId) }
                .willAnswer { throw Exception() }

            // WHEN
            val result = callSuspend { dataSource.getNote(mockNoteId) }

            // THEN
            result.shouldBeAnInstanceOf<Failure>()
            result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
        }

        @Test
        fun `GIVEN api returns successfully WHEN getNote is called THEN Success is returned`() {
            // GIVEN
            val mockNoteDto = mock<NoteDto>()
            val mockNote = mock<Note>()

            givenSuspend { mockApi.getNote(mockNoteId) }
                .willReturn(mockNoteDto)
            given(mockNoteDtoMapper.map(mockNoteDto))
                .willReturn(mockNote)

            // WHEN
            val result = callSuspend { dataSource.getNote(mockNoteId) }

            // THEN
            result.getOrNull() shouldBe mockNote
        }
    }
}
