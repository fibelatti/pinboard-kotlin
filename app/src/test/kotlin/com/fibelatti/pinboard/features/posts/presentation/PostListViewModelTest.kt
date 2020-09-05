package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
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
import com.fibelatti.pinboard.shouldNeverReceiveValues
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class PostListViewModelTest : BaseViewModelTest() {

    private val mockGetAllPosts = mockk<GetAllPosts>()
    private val mockGetRecentPosts = mockk<GetRecentPosts>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)

    private val mockSortType = mockk<SortType>()
    private val mockSearchTerm = "term"
    private val mockOffset = 12

    private val mockResponse = mockk<PostListResult>()
    private val mockException = Exception()

    private val postListViewModel = spyk(
        PostListViewModel(
            mockGetAllPosts,
            mockGetRecentPosts,
            mockAppStateRepository
        )
    )

    @Nested
    inner class LoadContentTest {

        @Test
        fun `GIVEN should load is Loaded WHEN loadContent is called THEN nothing else is called`() {
            // GIVEN
            val contentToLoad = PostListContent(
                category = mockk(),
                posts = null,
                showDescription = false,
                sortType = mockk(),
                searchParameters = mockk(),
                shouldLoad = Loaded
            )

            // WHEN
            postListViewModel.loadContent(contentToLoad)

            // THEN
            verify { postListViewModel.loadContent(contentToLoad) }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class OffsetTests {

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is All THEN getAll should be called`(
                testCase: Pair<ShouldLoad, Int>
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getAll(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(All, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getAll(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Public THEN getPublic should be called`(
                testCase: Pair<ShouldLoad, Int>
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getPublic(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Public, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getPublic(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Private THEN getPrivate should be called`(
                testCase: Pair<ShouldLoad, Int>
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getPrivate(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Private, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getPrivate(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Unread THEN getUnread should be called`(
                testCase: Pair<ShouldLoad, Int>
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getUnread(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Unread, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getUnread(
                        mockSortType,
                        mockSearchTerm,
                        mockTags,
                        offset = expectedOffset
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Untagged THEN getUntagged should be called`(
                testCase: Pair<ShouldLoad, Int>
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getUntagged(
                        mockSortType,
                        mockSearchTerm,
                        offset = expectedOffset
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Untagged, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getUntagged(
                        mockSortType,
                        mockSearchTerm,
                        offset = expectedOffset
                    )
                }
            }

            fun testCases(): List<Pair<ShouldLoad, Int>> =
                mutableListOf<Pair<ShouldLoad, Int>>().apply {
                    add(ShouldLoadFirstPage to 0)
                    add(ShouldLoadNextPage(13) to 13)
                }
        }

        @Test
        fun `WHEN loadContent is called AND category is Recent THEN getRecent should be called`() {
            // GIVEN
            every {
                postListViewModel.getRecent(
                    mockSortType,
                    mockSearchTerm,
                    mockTags
                )
            } returns Unit

            // WHEN
            postListViewModel.loadContent(createContent(Recent, ShouldLoadFirstPage))

            // THEN
            verify { postListViewModel.getRecent(mockSortType, mockSearchTerm, mockTags) }
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
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getAll(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(mockTags),
                    offset = mockOffset
                )
            )
        }
    }

    @Test
    fun `GIVEN getRecentPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() {
        coEvery { mockGetRecentPosts(any()) } returns flowOf(Failure(mockException))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, mockTags)

        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        postListViewModel.error.currentValueShouldBe(mockException)
    }

    @Test
    fun `GIVEN getRecentPosts will succeed WHEN launchGetAll is called THEN repository will run SetPosts`() {
        coEvery { mockGetRecentPosts(any()) } returns flowOf(Success(mockResponse))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, mockTags)

        coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
        postListViewModel.error.shouldNeverReceiveValues()
    }

    @Test
    fun `WHEN getPublic is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getPublic(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(mockTags),
                    GetPostParams.Visibility.Public,
                    offset = mockOffset
                )
            )
        }
    }

    @Test
    fun `WHEN getPrivate is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getPrivate(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(mockTags),
                    GetPostParams.Visibility.Private,
                    offset = mockOffset
                )
            )
        }
    }

    @Test
    fun `WHEN getUnread is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getUnread(mockSortType, mockSearchTerm, mockTags, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(mockTags),
                    readLater = true,
                    offset = mockOffset
                )
            )
        }
    }

    @Test
    fun `WHEN getUntagged is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getUntagged(mockSortType, mockSearchTerm, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Untagged,
                    offset = mockOffset
                )
            )
        }
    }

    @Nested
    inner class LaunchGetAllTests {

        @Test
        fun `GIVEN getAllPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() {
            coEvery { mockGetAllPosts(GetPostParams()) } returns flowOf(Failure(mockException))

            postListViewModel.launchGetAll(GetPostParams())

            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
            postListViewModel.error.currentValueShouldBe(mockException)
        }

        @Test
        fun `GIVEN getAllPosts will succeed and offset is 0 WHEN launchGetAll is called THEN repository will run SetPosts`() {
            // GIVEN
            val params = GetPostParams(offset = 0)
            coEvery { mockGetAllPosts(params) } returns flowOf(Success(mockResponse))

            // WHEN
            postListViewModel.launchGetAll(params)

            // THEN
            coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
            postListViewModel.error.shouldNeverReceiveValues()
        }

        @Test
        fun `GIVEN getAllPosts will succeed and offset is not 0 WHEN launchGetAll is called THEN repository will run SetNextPostPage`() {
            // GIVEN
            val params = GetPostParams(offset = 1)
            coEvery { mockGetAllPosts(params) } returns flowOf(Success(mockResponse))

            // WHEN
            postListViewModel.launchGetAll(params)

            // THEN
            coVerify { mockAppStateRepository.runAction(SetNextPostPage(mockResponse)) }
            postListViewModel.error.shouldNeverReceiveValues()
        }
    }
}
