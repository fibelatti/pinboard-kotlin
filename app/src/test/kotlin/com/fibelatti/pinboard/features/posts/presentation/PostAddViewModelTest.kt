package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.extension.empty
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlInvalid
import com.fibelatti.pinboard.MockDataProvider.mockUrlTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostSaved
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.InvalidUrlException
import com.fibelatti.pinboard.features.prepareToReceiveMany
import com.fibelatti.pinboard.features.shouldHaveReceived
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never

internal class PostAddViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mock<AppStateRepository>()
    private val mockAddPost = mock<AddPost>()
    private val mockResourceProvider = mock<ResourceProvider>()

    private val postAddViewModel = PostAddViewModel(
        mockAppStateRepository,
        mockAddPost,
        mockResourceProvider
    )

    @BeforeEach
    fun setup() {
        given(mockResourceProvider.getString(R.string.validation_error_invalid_url))
            .willReturn("R.string.validation_error_invalid_url")
        given(mockResourceProvider.getString(R.string.validation_error_empty_url))
            .willReturn("R.string.validation_error_empty_url")
        given(mockResourceProvider.getString(R.string.validation_error_empty_title))
            .willReturn("R.string.validation_error_empty_title")
    }

    @Test
    fun `GIVEN url is blank WHEN saveLink is called THEN invalidUrlError will receive a value`() {
        // WHEN
        postAddViewModel.saveLink(
            url = "",
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        postAddViewModel.invalidUrlError.currentValueShouldBe("R.string.validation_error_empty_url")
        postAddViewModel.saved.shouldNeverReceiveValues()
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `GIVEN title is blank WHEN saveLink is called THEN invalidUrlTitleError will received a value`() {
        // WHEN
        postAddViewModel.saveLink(
            url = mockUrlValid,
            title = "",
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        postAddViewModel.invalidUrlTitleError.currentValueShouldBe("R.string.validation_error_empty_title")
        postAddViewModel.saved.shouldNeverReceiveValues()
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `GIVEN addPost returns InvalidUrlException WHEN saveLink is called THEN invalidUrlError will receive a value`() {
        // GIVEN
        givenSuspend {
            mockAddPost(AddPost.Params(
                url = mockUrlInvalid,
                title = mockUrlTitle,
                description = mockUrlDescription,
                private = true,
                readLater = true,
                tags = mockTags
            ))
        }.willReturn(Failure(InvalidUrlException()))

        val loadingObserver = postAddViewModel.loading.prepareToReceiveMany()

        // WHEN
        postAddViewModel.saveLink(
            url = mockUrlInvalid,
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        postAddViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        postAddViewModel.invalidUrlError.currentValueShouldBe("R.string.validation_error_invalid_url")
        postAddViewModel.invalidUrlTitleError.currentValueShouldBe(String.empty())
        postAddViewModel.saved.shouldNeverReceiveValues()
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `GIVEN addPost returns an error WHEN saveLink is called THEN error will receive a value`() {
        // GIVEN
        val error = Exception()
        givenSuspend {
            mockAddPost(AddPost.Params(
                url = mockUrlValid,
                title = mockUrlTitle,
                description = mockUrlDescription,
                private = true,
                readLater = true,
                tags = mockTags
            ))
        }.willReturn(Failure(error))

        val loadingObserver = postAddViewModel.loading.prepareToReceiveMany()

        // WHEN
        postAddViewModel.saveLink(
            url = mockUrlValid,
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        postAddViewModel.loading.shouldHaveReceived(loadingObserver, true, false)
        postAddViewModel.invalidUrlError.currentValueShouldBe(String.empty())
        postAddViewModel.invalidUrlTitleError.currentValueShouldBe(String.empty())
        postAddViewModel.error.currentValueShouldBe(error)
        postAddViewModel.saved.shouldNeverReceiveValues()
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `GIVEN addPost is successful WHEN saveLink is called THEN AppStateRepository should run PostSaved`() {
        // GIVEN
        val post = createPost()
        givenSuspend {
            mockAddPost(AddPost.Params(
                url = mockUrlValid,
                title = mockUrlTitle,
                description = mockUrlDescription,
                private = true,
                readLater = true,
                tags = mockTags
            ))
        }.willReturn(Success(post))

        val loadingObserver = postAddViewModel.loading.prepareToReceiveMany()

        // WHEN
        postAddViewModel.saveLink(
            url = mockUrlValid,
            title = mockUrlTitle,
            description = mockUrlDescription,
            private = true,
            readLater = true,
            tags = mockTags
        )

        // THEN
        postAddViewModel.loading.shouldHaveReceived(loadingObserver, true)
        postAddViewModel.invalidUrlError.currentValueShouldBe(String.empty())
        postAddViewModel.invalidUrlTitleError.currentValueShouldBe(String.empty())
        postAddViewModel.error.shouldNeverReceiveValues()
        postAddViewModel.saved.currentEventShouldBe(Unit)
        verifySuspend(mockAppStateRepository) { runAction(PostSaved(post)) }
    }
}
