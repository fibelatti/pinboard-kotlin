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
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import org.junit.jupiter.api.Nested
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
    private val mockOffset = 12

    private val mockResponse = mock<Pair<Int, List<Post>>>()
    private val mockException = Exception()

    private val postListViewModel = spy(PostListViewModel(
        mockGetAllPosts,
        mockGetRecentPosts,
        mockAppStateRepository
    ))

    @Nested
    inner class LoadContentTest {

        @Test
        fun `WHEN loadContent is called AND category is All THEN getAll should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getAll(mockSortType, mockSearchTerm, mockTags, offset = 0)

            // WHEN
            postListViewModel.loadContent(createContent(All))

            // THEN
            verify(postListViewModel).getAll(mockSortType, mockSearchTerm, mockTags, offset = 0)
        }

        @Test
        fun `WHEN loadContent is called AND category is Recent THEN getRecent should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getRecent(mockSortType, mockSearchTerm, mockTags)

            // WHEN
            postListViewModel.loadContent(createContent(Recent))

            // THEN
            verify(postListViewModel).getRecent(mockSortType, mockSearchTerm, mockTags)
        }

        @Test
        fun `WHEN loadContent is called AND category is Public THEN getPublic should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getPublic(mockSortType, mockSearchTerm, mockTags, offset = 0)

            // WHEN
            postListViewModel.loadContent(createContent(Public))

            // THEN
            verify(postListViewModel).getPublic(mockSortType, mockSearchTerm, mockTags, offset = 0)
        }

        @Test
        fun `WHEN loadContent is called AND category is Private THEN getPrivate should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getPrivate(mockSortType, mockSearchTerm, mockTags, offset = 0)

            // WHEN
            postListViewModel.loadContent(createContent(Private))

            // THEN
            verify(postListViewModel).getPrivate(mockSortType, mockSearchTerm, mockTags, offset = 0)
        }

        @Test
        fun `WHEN loadContent is called AND category is Unread THEN getUnread should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getUnread(mockSortType, mockSearchTerm, mockTags, offset = 0)

            // WHEN
            postListViewModel.loadContent(createContent(Unread))

            // THEN
            verify(postListViewModel).getUnread(mockSortType, mockSearchTerm, mockTags, offset = 0)
        }

        @Test
        fun `WHEN loadContent is called AND category is Untagged THEN getUntagged should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getUntagged(mockSortType, mockSearchTerm, offset = 0)

            // WHEN
            postListViewModel.loadContent(createContent(Untagged))

            // THEN
            verify(postListViewModel).getUntagged(mockSortType, mockSearchTerm, offset = 0)
        }

        private fun createContent(category: ViewCategory): PostList =
            PostList(
                category = category,
                title = "",
                posts = null,
                sortType = mockSortType,
                searchParameters = SearchParameters(term = mockSearchTerm, tags = mockTags),
                shouldLoad = true
            )
    }

    @Test
    fun `WHEN getAll is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getAll(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                offset = mockOffset
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

        postListViewModel.getPublic(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                GetPostParams.Visibility.Public,
                offset = mockOffset
            )
        )
    }

    @Test
    fun `WHEN getPrivate is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getPrivate(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                GetPostParams.Visibility.Private,
                offset = mockOffset
            )
        )
    }

    @Test
    fun `WHEN getUnread is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getUnread(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify(postListViewModel).launchGetAll(
            GetPostParams(
                mockSortType,
                mockSearchTerm,
                GetPostParams.Tags.Tagged(mockTags),
                readLater = true,
                offset = mockOffset
            )
        )
    }

    @Test
    fun `WHEN getUntagged is called THEN launchGetAll is called with the expected GetPostParams`() {
        willDoNothing().given(postListViewModel).launchGetAll(safeAny())

        postListViewModel.getUntagged(mockSortType, mockSearchTerm, mockOffset)

        verify(postListViewModel).launchGetAll(
            GetPostParams(mockSortType, mockSearchTerm, GetPostParams.Tags.Untagged, offset = mockOffset)
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
