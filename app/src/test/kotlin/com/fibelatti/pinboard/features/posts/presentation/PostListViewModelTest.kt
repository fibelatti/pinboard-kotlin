package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.verify
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito.never
import org.mockito.Mockito.spy

internal class PostListViewModelTest : BaseViewModelTest() {

    private val mockGetAllPosts = mock<GetAllPosts>()
    private val mockGetRecentPosts = mock<GetRecentPosts>()
    private val mockAppStateRepository = mock<AppStateRepository>()

    private val mockSortType = mock<SortType>()
    private val mockSearchTerm = "term"

    private val mockResponse = mock<Pair<Int, List<Post>>>()
    private val mockException = Exception()

    private val postListViewModel = spy(PostListViewModel(
        mockGetAllPosts,
        mockGetRecentPosts,
        mockAppStateRepository
    ))

    @Test
    fun `WHEN getAll is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getAll(mockSortType, mockSearchTerm, mockTags)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags)
            )
        )
    }

    @Test
    fun `GIVEN getRecentPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() {
        givenSuspend { mockGetRecentPosts(safeAny()) }
            .willReturn(Failure(mockException))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, mockTags)

        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
        postListViewModel.error.currentValueShouldBe(mockException)
    }

    @Test
    fun `GIVEN getRecentPosts will succeed WHEN launchGetAll is called THEN repository will run SetPosts`() {
        givenSuspend { mockGetRecentPosts(safeAny()) }
            .willReturn(Success(mockResponse))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, mockTags)

        verifySuspend(mockAppStateRepository) { runAction(SetPosts(mockResponse)) }
        postListViewModel.error.shouldNeverReceiveValues()
    }

    @Test
    fun `WHEN getPublic is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getPublic(mockSortType, mockSearchTerm, mockTags)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                GetPostParams.Visibility.Public
            )
        )
    }

    @Test
    fun `WHEN getPrivate is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getPrivate(mockSortType, mockSearchTerm, mockTags)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                GetPostParams.Visibility.Private
            )
        )
    }

    @Test
    fun `WHEN getUnread is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getUnread(mockSortType, mockSearchTerm, mockTags)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                readLater = true
            )
        )
    }

    @Test
    fun `WHEN getUntagged is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getUntagged(mockSortType, mockSearchTerm)

        verify(postListViewModel).launchGetAll(
            GetPostParams(mockSortType, mockSearchTerm, GetPostParams.Tags.Untagged)
        )
    }

    @Test
    fun `GIVEN getAllPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() {
        givenSuspend { mockGetAllPosts(GetPostParams()) }
            .willReturn(Failure(mockException))

        postListViewModel.launchGetAll(GetPostParams())

        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
        postListViewModel.error.currentValueShouldBe(mockException)
    }

    @Test
    fun `GIVEN getAllPosts will succeed WHEN launchGetAll is called THEN repository will run SetPosts`() {
        givenSuspend { mockGetAllPosts(GetPostParams()) }
            .willReturn(Success(mockResponse))

        postListViewModel.launchGetAll(GetPostParams())

        verifySuspend(mockAppStateRepository) { runAction(SetPosts(mockResponse)) }
        postListViewModel.error.shouldNeverReceiveValues()
    }
}
