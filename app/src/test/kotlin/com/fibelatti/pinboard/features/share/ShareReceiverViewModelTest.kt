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
import com.fibelatti.pinboard.features.posts.domain.usecase.AddPost
import com.fibelatti.pinboard.features.posts.domain.usecase.GetUrlTitle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never

internal class ShareReceiverViewModelTest : BaseViewModelTest() {

    private val mockGetUrlTitle = mock<GetUrlTitle>()
    private val mockAddPost = mock<AddPost>()
    private val mockResourceProvider = mock<ResourceProvider>()

    private val shareReceiverViewModel = ShareReceiverViewModel(
        mockGetUrlTitle,
        mockAddPost,
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
    fun `WHEN GetUrlTitle fails THEN failed should receive a value`() {
        // GIVEN
        givenSuspend { mockGetUrlTitle(mockUrlValid) }
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
        givenSuspend { mockGetUrlTitle(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend { mockAddPost(AddPost.Params(mockUrlValid, mockUrlValid)) }
            .willReturn(Failure(Exception()))

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        shareReceiverViewModel.failed.currentEventShouldBe("R.string.generic_msg_error")
    }

    @Test
    fun `WEHN saveUrl succeeds THEN saved should receive a value`() {
        // GIVEN
        givenSuspend { mockGetUrlTitle(mockUrlValid) }
            .willReturn(Success(mockUrlValid))
        givenSuspend { mockAddPost(AddPost.Params(mockUrlValid, mockUrlValid)) }
            .willReturn(Success(createPost()))

        // WHEN
        shareReceiverViewModel.saveUrl(mockUrlValid)

        // THEN
        shareReceiverViewModel.saved.currentEventShouldBe("R.string.posts_saved_feedback")
    }
}
