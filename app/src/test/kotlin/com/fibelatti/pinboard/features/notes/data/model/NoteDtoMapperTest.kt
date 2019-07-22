package com.fibelatti.pinboard.features.notes.data.model

import com.fibelatti.core.extension.empty
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.mockNoteId
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.notes.domain.model.Note
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

internal class NoteDtoMapperTest {

    private val mockDateFormatter = mock<DateFormatter>()

    private val mapper = NoteDtoMapper(mockDateFormatter)

    @Test
    fun `WHEN title is null THEN mapped title is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = mockNoteId,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null
            )
        )

        // THEN
        result.title shouldBe String.empty()
    }

    @Test
    fun `WHEN createAt is null THEN mapped createAt is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = mockNoteId,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null
            )
        )

        // THEN
        result.createdAt shouldBe String.empty()
    }

    @Test
    fun `WHEN updatedAt is null THEN mapped updatedAt is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = mockNoteId,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null
            )
        )

        // THEN
        result.updatedAt shouldBe String.empty()
    }

    @Test
    fun `WHEN text is null THEN mapped text is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = mockNoteId,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null
            )
        )

        // THEN
        result.text shouldBe String.empty()
    }

    @Test
    fun `WHEN values are not null THEN mapped values are set`() {
        // GIVEN
        val inputDate = "2019-07-22 21:00:00"
        val outputDate = "22/07/19 21:00:00"
        val mockTitle = "Some title"
        val mockText = "Some text"

        given(mockDateFormatter.notesFormatToDisplayFormat(inputDate))
            .willReturn(outputDate)

        // WHEN
        val result = mapper.map(
            NoteDto(
                id = mockNoteId,
                title = mockTitle,
                createdAt = inputDate,
                updatedAt = inputDate,
                text = mockText
            )
        )

        // THEN
        result shouldBe Note(
            id = mockNoteId,
            title = mockTitle,
            createdAt = outputDate,
            updatedAt = outputDate,
            text = mockText
        )

        verify(mockDateFormatter, times(2)).notesFormatToDisplayFormat(inputDate)
    }
}
