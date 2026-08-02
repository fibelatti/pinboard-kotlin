package com.fibelatti.pinboard.features.offline.domain

import com.fibelatti.pinboard.MockDataProvider
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SaveOfflineCopyTest {

    private val mockOfflineCopyBuilder = mockk<OfflineCopyBuilder>()
    private val mockOfflineCopyRepository = mockk<OfflineCopyRepository>()

    private val saveOfflineCopy = SaveOfflineCopy(
        offlineCopyBuilder = mockOfflineCopyBuilder,
        offlineCopyRepository = mockOfflineCopyRepository,
    )

    private val post = MockDataProvider.createPost()
    private val params = SaveOfflineCopy.Params(post = post, appMode = AppMode.PINBOARD)

    @Test
    fun `GIVEN the page cannot be captured WHEN invoked THEN Failure is returned`() = runTest {
        // GIVEN
        val expectedError = NoReadableContentException(post.url)
        coEvery {
            mockOfflineCopyBuilder.build(url = post.url, fallbackTitle = post.displayTitle)
        } returns Result.failure(expectedError)

        // WHEN
        val result = saveOfflineCopy(params)

        // THEN
        assertThat(result.exceptionOrNull()).isEqualTo(expectedError)
        // Nothing was captured, so nothing may be written.
        coVerify(exactly = 0) {
            mockOfflineCopyRepository.save(
                appMode = any(),
                bookmarkId = any(),
                url = any(),
                title = any(),
                html = any(),
                truncated = any(),
            )
        }
    }

    @Test
    fun `GIVEN the copy cannot be stored WHEN invoked THEN Failure is returned`() = runTest {
        // GIVEN
        val expectedError = java.io.IOException("No space left")
        coEvery {
            mockOfflineCopyBuilder.build(url = post.url, fallbackTitle = post.displayTitle)
        } returns Result.success(OfflineCopyBuilder.Output(html = HTML, truncated = false))
        coEvery {
            mockOfflineCopyRepository.save(
                appMode = AppMode.PINBOARD,
                bookmarkId = post.id,
                url = post.url,
                title = post.displayTitle,
                html = HTML,
                truncated = false,
            )
        } returns Result.failure(expectedError)

        // WHEN
        val result = saveOfflineCopy(params)

        // THEN the storage failure must surface rather than be reported as a saved copy
        assertThat(result.exceptionOrNull()).isEqualTo(expectedError)
    }

    @Test
    fun `GIVEN the capture succeeds WHEN invoked THEN the copy is stored and returned`() = runTest {
        // GIVEN
        val expected = mockk<OfflineCopy>()
        coEvery {
            mockOfflineCopyBuilder.build(url = post.url, fallbackTitle = post.displayTitle)
        } returns Result.success(OfflineCopyBuilder.Output(html = HTML, truncated = false))
        coEvery {
            mockOfflineCopyRepository.save(
                appMode = AppMode.PINBOARD,
                bookmarkId = post.id,
                url = post.url,
                title = post.displayTitle,
                html = HTML,
                truncated = false,
            )
        } returns Result.success(expected)

        // WHEN
        val result = saveOfflineCopy(params)

        // THEN
        assertThat(result.getOrNull()).isEqualTo(expected)
    }

    @Test
    fun `GIVEN the capture was truncated WHEN invoked THEN that is carried into the stored copy`() = runTest {
        // GIVEN the user is told which of the two happened, so the flag must not be dropped here
        val expected = mockk<OfflineCopy>()
        coEvery {
            mockOfflineCopyBuilder.build(url = post.url, fallbackTitle = post.displayTitle)
        } returns Result.success(OfflineCopyBuilder.Output(html = HTML, truncated = true))
        coEvery {
            mockOfflineCopyRepository.save(
                appMode = AppMode.PINBOARD,
                bookmarkId = post.id,
                url = post.url,
                title = post.displayTitle,
                html = HTML,
                truncated = true,
            )
        } returns Result.success(expected)

        // WHEN
        val result = saveOfflineCopy(params)

        // THEN
        assertThat(result.getOrNull()).isEqualTo(expected)
    }

    @Test
    fun `GIVEN a linkding account WHEN invoked THEN the copy is stored against that account`() = runTest {
        // GIVEN the app mode comes from the caller, so a mode switch mid-capture cannot move the
        // copy to the wrong account
        val expected = mockk<OfflineCopy>()
        coEvery {
            mockOfflineCopyBuilder.build(url = post.url, fallbackTitle = post.displayTitle)
        } returns Result.success(OfflineCopyBuilder.Output(html = HTML, truncated = false))
        coEvery {
            mockOfflineCopyRepository.save(
                appMode = AppMode.LINKDING,
                bookmarkId = post.id,
                url = post.url,
                title = post.displayTitle,
                html = HTML,
                truncated = false,
            )
        } returns Result.success(expected)

        // WHEN
        val result = saveOfflineCopy(SaveOfflineCopy.Params(post = post, appMode = AppMode.LINKDING))

        // THEN
        assertThat(result.getOrNull()).isEqualTo(expected)
        coVerify {
            mockOfflineCopyRepository.save(
                appMode = AppMode.LINKDING,
                bookmarkId = post.id,
                url = post.url,
                title = post.displayTitle,
                html = HTML,
                truncated = false,
            )
        }
    }

    private companion object {

        const val HTML = "<html><body>Article</body></html>"
    }
}
