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
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.SetNextPostPage
import com.fibelatti.pinboard.features.appstate.SetPosts
import com.fibelatti.pinboard.features.appstate.ShouldLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.verify
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.spy

internal class PostListViewModelTest : BaseViewModelTest() {

    private val mockGetAllPosts = mock<GetAllPosts>()
    private val mockGetRecentPosts = mock<GetRecentPosts>()
    private val mockAppStateRepository = mock<AppStateRepository>()

    private val mockSortType = mock<SortType>()
    private val mockSearchTerm = "term"
    private val mockOffset = 12

    private val mockResponse = mock<PostListResult>()
    private val mockException = Exception()

    private val postListViewModel = spy(PostListViewModel(
        mockGetAllPosts,
        mockGetRecentPosts,
        mockAppStateRepository
    ))

    @Nested
    inner class LoadContentTest {

        @Test
        fun `GIVEN should load is Loaded WHEN loadContent is called THEN nothing else is called`() {
            // GIVEN
            val contentToLoad = PostListContent(
                category = mock(),
                posts = null,
                showDescription = false,
                sortType = mock(),
                searchParameters = mock(),
                shouldLoad = Loaded
            )

            // WHEN
            postListViewModel.loadContent(contentToLoad)

            // THEN
            verify(postListViewModel).loadContent(contentToLoad)
            Mockito.verifyNoMoreInteractions(postListViewModel)
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class OffsetTests {

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is All THEN getAll should be called`(testCase: Pair<ShouldLoad, Int>) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                willDoNothing()
                    .given(postListViewModel).getAll(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)

                // WHEN
                postListViewModel.loadContent(createContent(All, shouldLoad))

                // THEN
                verify(postListViewModel).getAll(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Public THEN getPublic should be called`(testCase: Pair<ShouldLoad, Int>) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                willDoNothing()
                    .given(postListViewModel).getPublic(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)

                // WHEN
                postListViewModel.loadContent(createContent(Public, shouldLoad))

                // THEN
                verify(postListViewModel).getPublic(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Private THEN getPrivate should be called`(testCase: Pair<ShouldLoad, Int>) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                willDoNothing()
                    .given(postListViewModel).getPrivate(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)

                // WHEN
                postListViewModel.loadContent(createContent(Private, shouldLoad))

                // THEN
                verify(postListViewModel).getPrivate(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Unread THEN getUnread should be called`(testCase: Pair<ShouldLoad, Int>) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                willDoNothing()
                    .given(postListViewModel).getUnread(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)

                // WHEN
                postListViewModel.loadContent(createContent(Unread, shouldLoad))

                // THEN
                verify(postListViewModel).getUnread(mockSortType, mockSearchTerm, mockTags, offset = expectedOffset)
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Untagged THEN getUntagged should be called`(testCase: Pair<ShouldLoad, Int>) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                willDoNothing()
                    .given(postListViewModel).getUntagged(mockSortType, mockSearchTerm, offset = expectedOffset)

                // WHEN
                postListViewModel.loadContent(createContent(Untagged, shouldLoad))

                // THEN
                verify(postListViewModel).getUntagged(mockSortType, mockSearchTerm, offset = expectedOffset)
            }

            fun testCases(): List<Pair<ShouldLoad, Int>> = mutableListOf<Pair<ShouldLoad, Int>>().apply {
                add(ShouldLoadFirstPage to 0)
                add(ShouldLoadNextPage(13) to 13)
            }
        }

        @Test
        fun `WHEN loadContent is called AND category is Recent THEN getRecent should be called`() {
            // GIVEN
            willDoNothing()
                .given(postListViewModel).getRecent(mockSortType, mockSearchTerm, mockTags)

            // WHEN
            postListViewModel.loadContent(createContent(Recent, ShouldLoadFirstPage))

            // THEN
            verify(postListViewModel).getRecent(mockSortType, mockSearchTerm, mockTags)
        }

        private fun createContent(category: ViewCategory, shouldLoad: ShouldLoad): PostListContent =
            PostListContent(
                category = category,
                posts = null,
                showDescription = false,
                sortType = mockSortType,
                searchParameters = SearchParameters(term = mockSearchTerm, tags = mockTags),
                shouldLoad = shouldLoad
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
            .willReturn(flowOf(Failure(mockException)))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, mockTags)

        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
        postListViewModel.error.currentValueShouldBe(mockException)
    }

    @Test
    fun `GIVEN getRecentPosts will succeed WHEN launchGetAll is called THEN repository will run SetPosts`() {
        givenSuspend { mockGetRecentPosts(safeAny()) }
            .willReturn(flowOf(Success(mockResponse)))

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

    @Nested
    inner class LaunchGetAllTests {

        @Test
        fun `GIVEN getAllPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() {
            givenSuspend { mockGetAllPosts(GetPostParams()) }
                .willReturn(flowOf(Failure(mockException)))

            postListViewModel.launchGetAll(GetPostParams())

            verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
            postListViewModel.error.currentValueShouldBe(mockException)
        }

        @Test
        fun `GIVEN getAllPosts will succeed and offset is 0 WHEN launchGetAll is called THEN repository will run SetPosts`() {
            // GIVEN
            val params = GetPostParams(offset = 0)
            givenSuspend { mockGetAllPosts(params) }
                .willReturn(flowOf(Success(mockResponse)))

            // WHEN
            postListViewModel.launchGetAll(params)

            // THEN
            verifySuspend(mockAppStateRepository) { runAction(SetPosts(mockResponse)) }
            postListViewModel.error.shouldNeverReceiveValues()
        }

        @Test
        fun `GIVEN getAllPosts will succeed and offset is not 0 WHEN launchGetAll is called THEN repository will run SetNextPostPage`() {
            // GIVEN
            val params = GetPostParams(offset = 1)
            givenSuspend { mockGetAllPosts(params) }
                .willReturn(flowOf(Success(mockResponse)))

            // WHEN
            postListViewModel.launchGetAll(params)

            // THEN
            verifySuspend(mockAppStateRepository) { runAction(SetNextPostPage(mockResponse)) }
            postListViewModel.error.shouldNeverReceiveValues()
        }
    }
}
