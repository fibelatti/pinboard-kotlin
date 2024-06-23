package com.fibelatti.pinboard.features.notes.data.model

import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.test.MockDataProvider.MOCK_NOTE_ID
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class NoteDtoMapperTest {

    private val mockDateFormatter = mockk<DateFormatter>()

    private val mapper = NoteDtoMapper(mockDateFormatter)

    @Test
    fun `WHEN title is null THEN mapped title is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = MOCK_NOTE_ID,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null,
            ),
        )

        // THEN
        assertThat(result.title).isEmpty()
    }

    @Test
    fun `WHEN createAt is null THEN mapped createAt is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = MOCK_NOTE_ID,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null,
            ),
        )

        // THEN
        assertThat(result.createdAt).isEmpty()
    }

    @Test
    fun `WHEN updatedAt is null THEN mapped updatedAt is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = MOCK_NOTE_ID,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null,
            ),
        )

        // THEN
        assertThat(result.updatedAt).isEmpty()
    }

    @Test
    fun `WHEN text is null THEN mapped text is empty`() {
        // WHEN
        val result = mapper.map(
            NoteDto(
                id = MOCK_NOTE_ID,
                title = null,
                createdAt = null,
                updatedAt = null,
                text = null,
            ),
        )

        // THEN
        assertThat(result.text).isEmpty()
    }

    @Test
    fun `WHEN values are not null THEN mapped values are set`() {
        // GIVEN
        val inputDate = "2019-07-22 21:00:00"
        val outputDate = "22/07/19 21:00:00"
        val mockTitle = "Some title"
        val mockText = "Some text"

        every { mockDateFormatter.notesFormatToDisplayFormat(inputDate) } returns outputDate

        // WHEN
        val result = mapper.map(
            NoteDto(
                id = MOCK_NOTE_ID,
                title = mockTitle,
                createdAt = inputDate,
                updatedAt = inputDate,
                text = mockText,
            ),
        )

        // THEN
        assertThat(result).isEqualTo(
            Note(
                id = MOCK_NOTE_ID,
                title = mockTitle,
                createdAt = outputDate,
                updatedAt = outputDate,
                text = mockText,
            ),
        )

        verify(exactly = 2) { mockDateFormatter.notesFormatToDisplayFormat(inputDate) }
    }
}
