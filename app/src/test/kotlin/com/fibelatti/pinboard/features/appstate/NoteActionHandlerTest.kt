package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class NoteActionHandlerTest {

    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private val noteActionHandler = NoteActionHandler(
        mockConnectivityInfoProvider
    )

    val initialContent = NoteListContent(
        notes = mockk(),
        shouldLoad = false,
        isConnected = false,
        previousContent = mockk()
    )

    @Nested
    inner class RefreshNotesTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(RefreshNotes, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN updated content is returned`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns true

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(RefreshNotes, initialContent) }

            // THEN
            assertThat(result).isEqualTo(
                NoteListContent(
                    notes = initialContent.notes,
                    shouldLoad = true,
                    isConnected = true,
                    previousContent = initialContent.previousContent
                )
            )
            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class SetNotesTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(mockk<SetNotes>(), content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN updated content is returned`() {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns true

            // WHEN
            val newNotes: List<Note> = mockk()
            val result = runBlocking {
                noteActionHandler.runAction(SetNotes(newNotes), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                NoteListContent(
                    notes = newNotes,
                    shouldLoad = false,
                    isConnected = initialContent.isConnected,
                    previousContent = initialContent.previousContent
                )
            )
        }
    }

    @Nested
    inner class SetNoteTests {

        @Test
        fun `WHEN currentContent is not NoteDetailContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<NoteListContent>()

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(mockk<SetNote>(), content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is NoteDetailContent THEN updated content is returned`() {
            // WHEN
            val noteDetails: Note = mockk()
            val initialContent = NoteDetailContent(
                id = "some-id",
                note = Either.Left(true),
                isConnected = true,
                previousContent = mockk()
            )

            val result =
                runBlocking { noteActionHandler.runAction(SetNote(noteDetails), initialContent) }

            // THEN
            assertThat(result).isEqualTo(
                NoteDetailContent(
                    id = "some-id",
                    note = Either.Right(noteDetails),
                    isConnected = true,
                    previousContent = initialContent.previousContent
                )
            )
        }
    }
}
