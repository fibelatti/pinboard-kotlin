package com.fibelatti.pinboard.features.offline.domain

import app.cash.turbine.test
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.offline.OfflineCopyStorage
import com.fibelatti.pinboard.features.offline.data.OfflineCopiesDao
import com.fibelatti.pinboard.features.offline.data.OfflineCopyDto
import com.fibelatti.pinboard.features.offline.data.OfflineCopyDtoMapper
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class OfflineCopyRepositoryTest {

    @TempDir
    private lateinit var tempDir: File

    private val mockOfflineCopiesDao = mockk<OfflineCopiesDao>(relaxed = true)
    private val mockOfflineCopyStorage = mockk<OfflineCopyStorage>(relaxed = true)
    private val offlineCopyDtoMapper = OfflineCopyDtoMapper()
    private val mockDateFormatter = mockk<DateFormatter> {
        every { nowAsDataFormat() } returns SAMPLE_DATE
    }

    private val repository by lazy {
        OfflineCopyRepository(
            offlineCopiesDao = mockOfflineCopiesDao,
            offlineCopyStorage = mockOfflineCopyStorage,
            offlineCopyDtoMapper = offlineCopyDtoMapper,
            dateFormatter = mockDateFormatter,
        )
    }

    @Test
    fun `getOfflineCopies maps the rows from the dao`() = runTest {
        val dto = createDto()
        every { mockOfflineCopiesDao.getOfflineCopies(AppMode.PINBOARD.name) } returns flowOf(listOf(dto))

        repository.getOfflineCopies(AppMode.PINBOARD).test {
            assertThat(receivedItems()).containsExactly(listOf(offlineCopyDtoMapper.map(dto)))
        }
    }

    @Test
    fun `getOfflineCopy returns null when there is no row`() = runTest {
        coEvery { mockOfflineCopiesDao.getOfflineCopy(any(), any()) } returns null

        val result = repository.getOfflineCopy(appMode = AppMode.PINBOARD, bookmarkId = SAMPLE_ID)

        assertThat(result).isNull()
    }

    @Test
    fun `getOfflineCopy returns the copy when the file is there`() = runTest {
        val dto = createDto()
        coEvery { mockOfflineCopiesDao.getOfflineCopy(AppMode.PINBOARD.name, SAMPLE_ID) } returns dto
        every { mockOfflineCopyStorage.file(AppMode.PINBOARD, SAMPLE_ID) } returns existingFile()

        val result = repository.getOfflineCopy(appMode = AppMode.PINBOARD, bookmarkId = SAMPLE_ID)

        assertThat(result).isEqualTo(offlineCopyDtoMapper.map(dto))
    }

    @Test
    fun `getOfflineCopy drops the row when its file is gone`() = runTest {
        // GIVEN a row whose file was removed behind the app's back — returning it would offer the
        // user a copy that opens onto nothing.
        coEvery { mockOfflineCopiesDao.getOfflineCopy(AppMode.PINBOARD.name, SAMPLE_ID) } returns createDto()
        every { mockOfflineCopyStorage.file(AppMode.PINBOARD, SAMPLE_ID) } returns File(tempDir, "missing.html")

        // WHEN
        val result = repository.getOfflineCopy(appMode = AppMode.PINBOARD, bookmarkId = SAMPLE_ID)

        // THEN
        assertThat(result).isNull()
        coVerify { mockOfflineCopiesDao.deleteOfflineCopy(AppMode.PINBOARD.name, SAMPLE_ID) }
    }

    @Test
    fun `save writes the file before the row`() = runTest {
        // GIVEN the row is what the rest of the app trusts, so it must never exist without a file.
        // The content is 18 bytes, asserted as a literal below so that recording the length of the
        // html rather than of the file on disk is caught.
        val file = existingFile(content = "<html>saved</html>")
        coEvery { mockOfflineCopyStorage.write(AppMode.PINBOARD, SAMPLE_ID, "<html>saved</html>") } returns file

        // WHEN
        val result = repository.save(
            appMode = AppMode.PINBOARD,
            bookmarkId = SAMPLE_ID,
            url = SAMPLE_URL,
            title = SAMPLE_TITLE,
            html = "<html>saved</html>",
            truncated = true,
        )

        // THEN
        val expected = OfflineCopy(
            bookmarkId = SAMPLE_ID,
            appMode = AppMode.PINBOARD,
            url = SAMPLE_URL,
            title = SAMPLE_TITLE,
            fileName = "$SAMPLE_ID.html",
            sizeBytes = 18L,
            dateCreated = SAMPLE_DATE,
            truncated = true,
        )
        assertThat(result.getOrNull()).isEqualTo(expected)

        coVerifyOrder {
            mockOfflineCopyStorage.write(AppMode.PINBOARD, SAMPLE_ID, "<html>saved</html>")
            mockOfflineCopiesDao.saveOfflineCopy(any())
        }
    }

    @Test
    fun `save fails without writing a row when the file cannot be written`() = runTest {
        coEvery { mockOfflineCopyStorage.write(any(), any(), any()) } throws java.io.IOException("No space left")

        val result = repository.save(
            appMode = AppMode.PINBOARD,
            bookmarkId = SAMPLE_ID,
            url = SAMPLE_URL,
            title = SAMPLE_TITLE,
            html = "<html/>",
            truncated = false,
        )

        assertThat(result.exceptionOrNull()).isInstanceOf(java.io.IOException::class.java)
        coVerify(exactly = 0) { mockOfflineCopiesDao.saveOfflineCopy(any()) }
    }

    @Test
    fun `delete removes the row and the file together`() = runTest {
        repository.delete(appMode = AppMode.LINKDING, bookmarkId = SAMPLE_ID)

        coVerify {
            mockOfflineCopiesDao.deleteOfflineCopy(AppMode.LINKDING.name, SAMPLE_ID)
            mockOfflineCopyStorage.delete(AppMode.LINKDING, SAMPLE_ID)
        }
    }

    @Test
    fun `deleteAll clears the rows and the files of one app mode`() = runTest {
        repository.deleteAll(appMode = AppMode.PINBOARD)

        coVerify {
            mockOfflineCopiesDao.deleteAllOfflineCopies(AppMode.PINBOARD.name)
            mockOfflineCopyStorage.deleteAll(AppMode.PINBOARD)
        }
    }

    @Test
    fun `deleteEverything clears every app mode, rows before files`() = runTest {
        // GIVEN the rows go first throughout this class. Wiping the directory first and then failing
        // partway through the rows would leave rows pointing at files that no longer exist, which is
        // the one state the repository exists to prevent.
        val pinboard = createDto(bookmarkId = "one")
        val linkding = createDto(bookmarkId = "two", appMode = AppMode.LINKDING)
        coEvery { mockOfflineCopiesDao.getEveryOfflineCopy() } returns listOf(pinboard, linkding)

        repository.deleteEverything()

        coVerifyOrder {
            mockOfflineCopiesDao.deleteOfflineCopy(AppMode.PINBOARD.name, "one")
            mockOfflineCopiesDao.deleteOfflineCopy(AppMode.LINKDING.name, "two")
            mockOfflineCopyStorage.deleteEverything()
        }
    }

    @Test
    fun `deleteOrphaned drops the copies whose bookmark is gone`() = runTest {
        coEvery { mockOfflineCopiesDao.getOrphanedPinboardCopyIds(AppMode.PINBOARD.name) } returns listOf("gone")
        coEvery { mockOfflineCopiesDao.getAllOfflineCopies(AppMode.PINBOARD.name) } returns listOf(createDto())

        repository.deleteOrphaned(appMode = AppMode.PINBOARD)

        coVerify {
            mockOfflineCopiesDao.deleteOfflineCopy(AppMode.PINBOARD.name, "gone")
            mockOfflineCopyStorage.delete(AppMode.PINBOARD, "gone")
        }
    }

    @Test
    fun `deleteOrphaned then sweeps the files left without a row`() = runTest {
        coEvery { mockOfflineCopiesDao.getOrphanedPinboardCopyIds(any()) } returns emptyList()
        coEvery { mockOfflineCopiesDao.getAllOfflineCopies(AppMode.PINBOARD.name) } returns listOf(
            createDto(bookmarkId = "one"),
            createDto(bookmarkId = "two"),
        )

        repository.deleteOrphaned(appMode = AppMode.PINBOARD)

        coVerify {
            mockOfflineCopyStorage.deleteFilesNotIn(
                appMode = AppMode.PINBOARD,
                fileNames = setOf("one.html", "two.html"),
            )
        }
    }

    @Test
    fun `deleteOrphaned uses the linkding table for linkding`() = runTest {
        coEvery { mockOfflineCopiesDao.getOrphanedLinkdingCopyIds(AppMode.LINKDING.name) } returns listOf("gone")
        coEvery { mockOfflineCopiesDao.getAllOfflineCopies(AppMode.LINKDING.name) } returns emptyList()

        repository.deleteOrphaned(appMode = AppMode.LINKDING)

        coVerify { mockOfflineCopiesDao.deleteOfflineCopy(AppMode.LINKDING.name, "gone") }
        coVerify(exactly = 0) { mockOfflineCopiesDao.getOrphanedPinboardCopyIds(any()) }
    }

    @Test
    fun `deleteOrphaned uses the pinboard table for app review mode`() = runTest {
        // Both modes share the same bookmark table, so review mode must not be matched against
        // Linkding's — every copy would look orphaned and be deleted.
        coEvery { mockOfflineCopiesDao.getOrphanedPinboardCopyIds(AppMode.NO_API.name) } returns emptyList()
        coEvery { mockOfflineCopiesDao.getAllOfflineCopies(AppMode.NO_API.name) } returns emptyList()

        repository.deleteOrphaned(appMode = AppMode.NO_API)

        coVerify { mockOfflineCopiesDao.getOrphanedPinboardCopyIds(AppMode.NO_API.name) }
        coVerify(exactly = 0) { mockOfflineCopiesDao.getOrphanedLinkdingCopyIds(any()) }
    }

    @Test
    fun `deleteOrphaned does nothing when there is no account to act on`() = runTest {
        // UNSET means no backend is resolved yet, so there is no table to compare against.
        repository.deleteOrphaned(appMode = AppMode.UNSET)

        coVerify(exactly = 0) {
            mockOfflineCopiesDao.getOrphanedPinboardCopyIds(any())
            mockOfflineCopiesDao.getOrphanedLinkdingCopyIds(any())
            mockOfflineCopyStorage.deleteFilesNotIn(any(), any())
        }
    }

    @Test
    fun `totalSizeOnDisk is measured on disk rather than summed from the rows`() = runTest {
        // The clear-all preference deletes the directory outright, so files without a row count too.
        coEvery { mockOfflineCopyStorage.totalSize() } returns 4_096L

        assertThat(repository.totalSizeOnDisk()).isEqualTo(4_096L)
    }

    @Test
    fun `fileFor resolves against the copy's own app mode`() = runTest {
        // GIVEN a copy listed while another account is selected
        val offlineCopy = offlineCopyDtoMapper.map(createDto(appMode = AppMode.LINKDING))
        val expected = File(tempDir, "expected.html")
        every { mockOfflineCopyStorage.file(AppMode.LINKDING, SAMPLE_ID) } returns expected

        assertThat(repository.fileFor(offlineCopy)).isEqualTo(expected)
    }

    private fun existingFile(content: String = "<html/>"): File = File(tempDir, "$SAMPLE_ID.html").apply {
        writeText(content)
    }

    private fun createDto(
        bookmarkId: String = SAMPLE_ID,
        appMode: AppMode = AppMode.PINBOARD,
    ): OfflineCopyDto = OfflineCopyDto(
        bookmarkId = bookmarkId,
        appMode = appMode.name,
        url = SAMPLE_URL,
        title = SAMPLE_TITLE,
        fileName = "$bookmarkId.html",
        sizeBytes = 1_024L,
        dateCreated = SAMPLE_DATE,
        truncated = false,
    )

    private companion object {

        const val SAMPLE_ID = "sample-id"
        const val SAMPLE_URL = "https://example.com/article"
        const val SAMPLE_TITLE = "Article"
        const val SAMPLE_DATE = "2026-08-02T10:00:00Z"
    }
}
