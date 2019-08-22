package com.fibelatti.pinboard.features.share

import com.fibelatti.core.archcomponents.test.extension.currentEventShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostFromShare
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.ExtractUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.ParseUrl
import com.fibelatti.pinboard.features.posts.domain.usecase.RichUrl
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

internal class ShareReceiverViewModelTest : BaseViewModelTest() {

    private val mockExtractUrl = mock<ExtractUrl>()
    private val mockParseUrl = mock<ParseUrl>()
    private val mockAddPost = mock<AddPost>()
    private val mockUserRepository = mock<UserRepository>()
    private val mockAppStateRepository = mock<AppStateRepository>()
    private val mockResourceProvider = mock<ResourceProvider>()

    private val shareReceiverViewModel = ShareReceiverViewModel(
        mockExtractUrl,
        mockParseUrl,
        mockAddPost,
        mockUserRepository,
        mockAppStateRepository,
        mockResourceProvider
    )

    @BeforeEach
    fun setup() {
        given(mockResourceProvider.getString(R.string.posts_saved_feedback))
            .willReturn("R.string.posts_saved_feedback")
        given(mockResourceProvider.getString(R.string.generic_msg_error))
            .willReturn("R.string.generic_msg_error")
    }

    @Test
    fun `WHEN ExtractUrl fails THEN failed should receive a value`() {
        // GIVEN
        givenSuspend { mockExtractUrl(mockUrlValid) }
            .willReturn(Failure(Exception()))

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        shareReceiverViewModel.failed.currentEventShouldBe("R.string.generic_msg_error")
        verifySuspend(mockAddPost, never()) { invoke(safeAny()) }
    }

    @Test
    fun `WHEN ParseUrl fails THEN failed should receive a value`() {
        // GIVEN
        givenSuspend { mockExtractUrl(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend { mockParseUrl(mockUrlValid) }
            .willReturn(Failure(Exception()))

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        shareReceiverViewModel.failed.currentEventShouldBe("R.string.generic_msg_error")
        verifySuspend(mockAddPost, never()) { invoke(safeAny()) }
    }

    @Test
    fun `WHEN AddPost fails THEN failed should receive a value`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        givenSuspend { mockExtractUrl(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend { mockParseUrl(mockUrlValid) }
            .willReturn(Success(RichUrl(mockUrlValid, mockUrlValid)))
        given(mockUserRepository.getDefaultPrivate())
            .willReturn(defaultPrivate)
        given(mockUserRepository.getDefaultReadLater())
            .willReturn(defaultReadLater)
        givenSuspend {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    private = defaultPrivate,
                    readLater = defaultReadLater
                )
            )
        }.willReturn(Failure(Exception()))

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify(mockUserRepository).getDefaultPrivate()
        verify(mockUserRepository).getDefaultReadLater()
        shareReceiverViewModel.failed.currentEventShouldBe("R.string.generic_msg_error")
    }

    @Test
    fun `GIVEN getEditAfterSharing is false WHEN saveUrl succeeds THEN saved should receive a value`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        givenSuspend { mockExtractUrl(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend { mockParseUrl(mockUrlValid) }
            .willReturn(Success(RichUrl(mockUrlValid, mockUrlValid)))
        given(mockUserRepository.getDefaultPrivate())
            .willReturn(defaultPrivate)
        given(mockUserRepository.getDefaultReadLater())
            .willReturn(defaultReadLater)

        givenSuspend {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    private = defaultPrivate,
                    readLater = defaultReadLater
                )
            )
        }.willReturn(Success(createPost()))

        given(mockUserRepository.getEditAfterSharing())
            .willReturn(false)

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify(mockUserRepository).getDefaultPrivate()
        verify(mockUserRepository).getDefaultReadLater()
        shareReceiverViewModel.saved.currentEventShouldBe("R.string.posts_saved_feedback")
    }

    @Test
    fun `GIVEN getEditAfterSharing is true WHEN saveUrl succeeds THEN edit should receive a value`() {
        // GIVEN
        val defaultPrivate = randomBoolean()
        val defaultReadLater = randomBoolean()

        givenSuspend { mockExtractUrl(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend { mockParseUrl(mockUrlValid) }
            .willReturn(Success(RichUrl(mockUrlValid, mockUrlValid)))
        given(mockUserRepository.getDefaultPrivate())
            .willReturn(defaultPrivate)
        given(mockUserRepository.getDefaultReadLater())
            .willReturn(defaultReadLater)

        val post = createPost()
        givenSuspend {
            mockAddPost(
                AddPost.Params(
                    url = mockUrlValid,
                    title = mockUrlValid,
                    private = defaultPrivate,
                    readLater = defaultReadLater
                )
            )
        }.willReturn(Success(post))

        given(mockUserRepository.getEditAfterSharing())
            .willReturn(true)

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        verify(mockUserRepository).getDefaultPrivate()
        verify(mockUserRepository).getDefaultReadLater()
        shareReceiverViewModel.edit.currentEventShouldBe("R.string.posts_saved_feedback")
        verifySuspend(mockAppStateRepository) { runAction(EditPostFromShare(post)) }
    }
}
