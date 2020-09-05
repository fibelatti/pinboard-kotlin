package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.pinboard.shouldNeverReceiveValues
import com.fibelatti.core.extension.empty
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTagString1
import com.fibelatti.pinboard.MockDataProvider.mockTagString2
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlInvalid
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.prepareToReceiveMany
import com.fibelatti.pinboard.shouldHaveReceived
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class EditPostViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mockk<AppStateRepository>()
    private val mockGetSuggestedTags = mockk<GetSuggestedTags>()
    private val mockAddPost = mockk<AddPost>()
    private val mockResourceProvider = mockk<ResourceProvider> {
        every { getString(R.string.validation_error_invalid_url) } returns "R.string.validation_error_invalid_url"
        every { getString(R.string.validation_error_empty_url) } returns "R.string.validation_error_empty_url"
        every { getString(R.string.validation_error_empty_title) } returns "R.string.validation_error_empty_title"
    }

    private val editPostViewModel = EditPostViewModel(
        mockAppStateRepository,
        mockGetSuggestedTags,
        mockAddPost,
        mockResourceProvider
    )

    @Test
    fun `GIVEN getSuggestedTags will fail WHEN searchForTag is called THEN suggestedTags should never receive values`() {
        // GIVEN
        coEvery { mockGetSuggestedTags(any()) } returns Failure(Exception())

        // WHEN
        editPostViewModel.searchForTag(mockTagString1, mockk())

        // THEN
        editPostViewModel.suggestedTags.shouldNeverReceiveValues()
    }

    @Test
    fun `GIVEN getSuggestedTags will succeed WHEN searchForTag is called THEN suggestedTags should receive its response`() {
        // GIVEN
        val result = listOf(mockTagString1, mockTagString2)
        coEvery { mockGetSuggestedTags(any()) } returns Success(result)

        // WHEN
        editPostViewModel.searchForTag(mockTagString1, mockk())

        // THEN
        editPostViewModel.suggestedTags.currentValueShouldBe(result)
    }

    @Test
    fun `GIVEN url is blank WHEN saveLink is called THEN invalidUrlError will receive a value`() {
        // WHEN
        editPostViewModel.saveLink(
            url = "",
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        editPostViewModel.invalidUrlError.currentValueShouldBe("R.string.validation_error_empty_url")
        editPostViewModel.saved.shouldNeverReceiveValues()
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN title is blank WHEN saveLink is called THEN invalidUrlTitleError will received a value`() {
        // WHEN
        editPostViewModel.saveLink(
            url = mockUrlValid,
            title = "",
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        editPostViewModel.invalidUrlTitleError.currentValueShouldBe("R.string.validation_error_empty_title")
        editPostViewModel.saved.shouldNeverReceiveValues()
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN addPost returns InvalidUrlException WHEN saveLink is called THEN invalidUrlError will receive a value`() {
        // GIVEN
        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlInvalid,
                    title = mockUrlTitle,
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )
        } returns Failure(InvalidUrlException())

        val loadingObserver = editPostViewModel.loading.prepareToReceiveMany()

        // WHEN
        editPostViewModel.saveLink(
            url = mockUrlInvalid,
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        editPostViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        editPostViewModel.invalidUrlError.currentValueShouldBe("R.string.validation_error_invalid_url")
        editPostViewModel.invalidUrlTitleError.currentValueShouldBe(String.empty())
        editPostViewModel.saved.shouldNeverReceiveValues()
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN addPost returns an error WHEN saveLink is called THEN error will receive a value`() {
        // GIVEN
        val error = Exception()
        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )
        } returns Failure(error)

        val loadingObserver = editPostViewModel.loading.prepareToReceiveMany()

        // WHEN
        editPostViewModel.saveLink(
            url = mockUrlValid,
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        editPostViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        editPostViewModel.invalidUrlError.currentValueShouldBe(String.empty())
        editPostViewModel.invalidUrlTitleError.currentValueShouldBe(String.empty())
        editPostViewModel.error.currentValueShouldBe(error)
        editPostViewModel.saved.shouldNeverReceiveValues()
        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
    }

    @Test
    fun `GIVEN addPost is successful WHEN saveLink is called THEN AppStateRepository should run PostSaved`() {
        // GIVEN
        val post = createPost()
        coEvery {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlTitle,
                    description = mockUrlDescription,
                    private = true,
                    readLater = true,
                    tags = mockTags
                )
            )
        } returns Success(post)

        val loadingObserver = editPostViewModel.loading.prepareToReceiveMany()

        // WHEN
        editPostViewModel.saveLink(
            url = mockUrlValid,
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        editPostViewModel.loading.shouldHaveReceived(loadingObserver, true)
        editPostViewModel.invalidUrlError.currentValueShouldBe(String.empty())
        editPostViewModel.invalidUrlTitleError.currentValueShouldBe(String.empty())
        editPostViewModel.error.shouldNeverReceiveValues()
        editPostViewModel.saved.currentEventShouldBe(Unit)
        coVerify { mockAppStateRepository.runAction(PostSaved(post)) }
    }
}
