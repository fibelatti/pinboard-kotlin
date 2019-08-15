package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.notes.domain.model.Note
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

internal class NoteActionHandlerTest {

    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private val noteActionHandler = NoteActionHandler(
        mockConnectivityInfoProvider
    )

    val initialContent = NoteListContent(
        notes = mock(),
        shouldLoad = false,
        isConnected = false,
        previousContent = mock()
    )

    @Nested
    inner class RefreshNotesTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(RefreshNotes, content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(RefreshNotes, initialContent) }

            // THEN
            result shouldBe NoteListContent(
                notes = initialContent.notes,
                shouldLoad = true,
                isConnected = true,
                previousContent = initialContent.previousContent
            )
            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }

    @Nested
    inner class SetNotesTests {

        @Test
        fun `WHEN currentContent is not NoteListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(mock<SetNotes>(), content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is NoteListContent THEN updated content is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(true)

            // WHEN
            val newNotes: List<Note> = mock()
            val result = runBlocking {
                noteActionHandler.runAction(SetNotes(newNotes), initialContent)
            }

            // THEN
            result shouldBe NoteListContent(
                notes = newNotes,
                shouldLoad = false,
                isConnected = initialContent.isConnected,
                previousContent = initialContent.previousContent
            )
        }
    }

    @Nested
    inner class SetNoteTests {

        @Test
        fun `WHEN currentContent is not NoteDetailContent THEN same content is returned`() {
            // GIVEN
            val content = mock<NoteListContent>()

            // WHEN
            val result = runBlocking { noteActionHandler.runAction(mock<SetNote>(), content) }

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is NoteDetailContent THEN updated content is returned`() {
            // WHEN
            val noteDetails: Note = mock()
            val initialContent = NoteDetailContent(
                id = "some-id",
                note = Either.Left(true),
                isConnected = true,
                previousContent = mock()
            )

            val result = runBlocking { noteActionHandler.runAction(SetNote(noteDetails), initialContent) }

            // THEN
            result shouldBe NoteDetailContent(
                id = "some-id",
                note = Either.Right(noteDetails),
                isConnected = true,
                previousContent = initialContent.previousContent
            )
        }
    }
}
