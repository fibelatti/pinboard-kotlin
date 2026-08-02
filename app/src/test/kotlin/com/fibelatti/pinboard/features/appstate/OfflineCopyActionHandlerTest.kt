package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class OfflineCopyActionHandlerTest {

    private val offlineCopyActionHandler = OfflineCopyActionHandler()

    private val offlineCopies: List<OfflineCopy> = listOf(
        createOfflineCopy(bookmarkId = "one", sizeBytes = 1_024L),
        createOfflineCopy(bookmarkId = "two", sizeBytes = 1_024L),
    )

    private val initialContent = OfflineCopyListContent(
        offlineCopies = emptyList(),
        totalSize = 0,
        shouldLoad = true,
        previousContent = mockk(),
    )

    @Nested
    inner class SetOfflineCopiesTests {

        @Test
        fun `WHEN currentContent is not OfflineCopiesContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = offlineCopyActionHandler.runAction(SetOfflineCopies(offlineCopies), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is OfflineCopiesContent THEN the copies are set AND loading stops`() = runTest {
            // WHEN
            val result = offlineCopyActionHandler.runAction(SetOfflineCopies(offlineCopies), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                initialContent.copy(
                    offlineCopies = offlineCopies,
                    totalSize = 2_048L,
                    shouldLoad = false,
                ),
            )
        }

        @Test
        fun `WHEN the copies are set THEN the total covers only the copies being shown`() = runTest {
            // GIVEN the list is scoped to one account, so the size has to be too — measuring the
            // directory instead would report every account's copies against this account's count.
            val copies = listOf(
                createOfflineCopy(bookmarkId = "one", sizeBytes = 300L),
                createOfflineCopy(bookmarkId = "two", sizeBytes = 700L),
            )

            // WHEN
            val result = offlineCopyActionHandler.runAction(SetOfflineCopies(copies), initialContent)

            // THEN
            assertThat((result as OfflineCopyListContent).totalSize).isEqualTo(1_000L)
        }

        @Test
        fun `WHEN there are no copies THEN loading still stops`() = runTest {
            // WHEN an empty result must be distinguishable from one that has not loaded yet,
            // otherwise the list would show a spinner forever instead of its empty state.
            val result = offlineCopyActionHandler.runAction(SetOfflineCopies(emptyList()), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                initialContent.copy(
                    offlineCopies = emptyList(),
                    totalSize = 0L,
                    shouldLoad = false,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is OfflineCopyDetailContent THEN the list behind it is updated`() = runTest {
            // GIVEN
            val offlineCopy = createOfflineCopy(bookmarkId = "open", sizeBytes = 1L)
            val content = OfflineCopyDetailContent(
                offlineCopy = offlineCopy,
                previousContent = initialContent,
            )

            // WHEN
            val result = offlineCopyActionHandler.runAction(SetOfflineCopies(offlineCopies), content)

            // THEN the copy being viewed is untouched, but going back must not land on a stale list.
            assertThat(result).isEqualTo(
                OfflineCopyDetailContent(
                    offlineCopy = offlineCopy,
                    previousContent = initialContent.copy(
                        offlineCopies = offlineCopies,
                        totalSize = 2_048L,
                        shouldLoad = false,
                    ),
                ),
            )
        }
    }

    private fun createOfflineCopy(bookmarkId: String, sizeBytes: Long): OfflineCopy = OfflineCopy(
        bookmarkId = bookmarkId,
        appMode = AppMode.PINBOARD,
        url = "https://example.com/$bookmarkId",
        title = "Title $bookmarkId",
        fileName = "$bookmarkId.html",
        sizeBytes = sizeBytes,
        dateCreated = "2026-08-02T10:00:00Z",
        truncated = false,
    )
}
