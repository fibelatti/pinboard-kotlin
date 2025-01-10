package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
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
import com.fibelatti.pinboard.features.appstate.ShouldForceLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoad
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.ShouldLoadNextPage
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.pinboard.isEmpty
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class PostListViewModelTest : BaseViewModelTest() {

    private val mockGetAllPosts = mockk<GetAllPosts>()
    private val mockGetRecentPosts = mockk<GetRecentPosts>()
    private val mockAppStateRepository = mockk<AppStateRepository>(relaxed = true)
    private val savedFiltersRepository = mockk<SavedFiltersRepository>()

    private val mockSortType = mockk<SortType>()
    private val mockSearchTerm = "term"
    private val mockOffset = 12

    private val mockResponse = mockk<PostListResult>()
    private val mockException = Exception()

    private val postListViewModel = spyk(
        PostListViewModel(
            getAllPosts = mockGetAllPosts,
            getRecentPosts = mockGetRecentPosts,
            appStateRepository = mockAppStateRepository,
            savedFiltersRepository = savedFiltersRepository,
        ),
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
                shouldLoad = Loaded,
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
                testCase: Pair<ShouldLoad, Int>,
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getAll(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                        forceRefresh = shouldLoad is ShouldForceLoad,
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(All, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getAll(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                        forceRefresh = shouldLoad is ShouldForceLoad,
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Public THEN getPublic should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getPublic(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Public, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getPublic(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Private THEN getPrivate should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getPrivate(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Private, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getPrivate(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Unread THEN getUnread should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getUnread(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Unread, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getUnread(
                        mockSortType,
                        mockSearchTerm,
                        SAMPLE_TAGS,
                        offset = expectedOffset,
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Untagged THEN getUntagged should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase
                every {
                    postListViewModel.getUntagged(
                        mockSortType,
                        mockSearchTerm,
                        offset = expectedOffset,
                    )
                } returns Unit

                // WHEN
                postListViewModel.loadContent(createContent(Untagged, shouldLoad))

                // THEN
                verify {
                    postListViewModel.getUntagged(
                        mockSortType,
                        mockSearchTerm,
                        offset = expectedOffset,
                    )
                }
            }

            fun testCases(): List<Pair<ShouldLoad, Int>> =
                mutableListOf<Pair<ShouldLoad, Int>>().apply {
                    add(ShouldForceLoad to 0)
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
                    SAMPLE_TAGS,
                )
            } returns Unit

            // WHEN
            postListViewModel.loadContent(createContent(Recent, ShouldLoadFirstPage))

            // THEN
            verify { postListViewModel.getRecent(mockSortType, mockSearchTerm, SAMPLE_TAGS) }
        }

        private fun createContent(category: ViewCategory, shouldLoad: ShouldLoad): PostListContent =
            PostListContent(
                category = category,
                posts = null,
                showDescription = false,
                sortType = mockSortType,
                searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                shouldLoad = shouldLoad,
            )
    }

    @Test
    fun `WHEN getAll is called THEN launchGetAll is called with the expected GetPostParams`() {
        val force = randomBoolean()
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getAll(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset, force)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                    offset = mockOffset,
                    forceRefresh = force,
                ),
            )
        }
    }

    @Test
    fun `GIVEN getRecentPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() = runTest {
        every { mockGetRecentPosts(any()) } returns flowOf(Failure(mockException))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, SAMPLE_TAGS)

        coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
        assertThat(postListViewModel.error.first()).isEqualTo(mockException)
    }

    @Test
    fun `GIVEN getRecentPosts will succeed WHEN launchGetAll is called THEN repository will run SetPosts`() = runTest {
        every { mockGetRecentPosts(any()) } returns flowOf(Success(mockResponse))

        postListViewModel.getRecent(mockSortType, mockSearchTerm, SAMPLE_TAGS)

        coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
        assertThat(postListViewModel.error.isEmpty()).isTrue()
    }

    @Test
    fun `WHEN getPublic is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getPublic(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                    PostVisibility.Public,
                    offset = mockOffset,
                ),
            )
        }
    }

    @Test
    fun `WHEN getPrivate is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getPrivate(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                    PostVisibility.Private,
                    offset = mockOffset,
                ),
            )
        }
    }

    @Test
    fun `WHEN getUnread is called THEN launchGetAll is called with the expected GetPostParams`() {
        every { postListViewModel.launchGetAll(any()) } returns Unit

        postListViewModel.getUnread(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset)

        verify {
            postListViewModel.launchGetAll(
                GetPostParams(
                    mockSortType,
                    mockSearchTerm,
                    GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                    readLater = true,
                    offset = mockOffset,
                ),
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
                    offset = mockOffset,
                ),
            )
        }
    }

    @Nested
    inner class LaunchGetAllTests {

        @Test
        fun `GIVEN getAllPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() =
            runTest {
                every { mockGetAllPosts(GetPostParams()) } returns flowOf(Failure(mockException))

                postListViewModel.launchGetAll(GetPostParams())

                coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
                assertThat(postListViewModel.error.first()).isEqualTo(mockException)
            }

        @Test
        fun `GIVEN getAllPosts will succeed and offset is 0 WHEN launchGetAll is called THEN repository will run SetPosts`() =
            runTest {
                // GIVEN
                val params = GetPostParams(offset = 0)
                every { mockGetAllPosts(params) } returns flowOf(Success(mockResponse))

                // WHEN
                postListViewModel.launchGetAll(params)

                // THEN
                coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
                assertThat(postListViewModel.error.isEmpty()).isTrue()
            }

        @Test
        fun `GIVEN getAllPosts will succeed and offset is not 0 WHEN launchGetAll is called THEN repository will run SetNextPostPage`() =
            runTest {
                // GIVEN
                val params = GetPostParams(offset = 1)
                every { mockGetAllPosts(params) } returns flowOf(Success(mockResponse))

                // WHEN
                postListViewModel.launchGetAll(params)

                // THEN
                coVerify { mockAppStateRepository.runAction(SetNextPostPage(mockResponse)) }
                assertThat(postListViewModel.error.isEmpty()).isTrue()
            }
    }

    @Test
    fun `when saveFilter is called then it calls the repository`() = runTest {
        val savedFilter = mockk<SavedFilter>()

        coJustRun { savedFiltersRepository.saveFilter(savedFilter) }

        postListViewModel.saveFilter(savedFilter)

        coVerify { savedFiltersRepository.saveFilter(savedFilter) }
    }
}
