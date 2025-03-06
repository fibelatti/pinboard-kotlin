package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.MockDataProvider.createLoginContent
import com.fibelatti.pinboard.MockDataProvider.createPostListContent
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostDetailContent
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
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.posts.domain.usecase.GetAllPosts
import com.fibelatti.pinboard.features.posts.domain.usecase.GetPostParams
import com.fibelatti.pinboard.features.posts.domain.usecase.GetRecentPosts
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class PostListViewModelTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState(content = createLoginContent()))
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
        coJustRun { runAction(any()) }
    }

    private val mockResponse = mockk<PostListResult>()
    private val mockException = Exception()

    private val mockGetAllPosts = mockk<GetAllPosts> {
        every { this@mockk.invoke(any()) } returns flowOf(Success(mockResponse))
    }
    private val mockGetRecentPosts = mockk<GetRecentPosts> {
        every { this@mockk.invoke(any()) } returns flowOf(Success(mockResponse))
    }
    private val savedFiltersRepository = mockk<SavedFiltersRepository>()

    private val mockSortType = mockk<SortType>()
    private val mockSearchTerm = "term"
    private val mockOffset = 12

    private val postListViewModel = PostListViewModel(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        savedFiltersRepository = savedFiltersRepository,
        getAllPosts = mockGetAllPosts,
        getRecentPosts = mockGetRecentPosts,
    )

    @Nested
    inner class InitTests {

        @Test
        fun `WHEN PostListContent is emitted AND should load content is true THEN loadContent should be called`() =
            runTest {
                // GIVEN
                val postListContent = createPostListContent(shouldLoad = ShouldLoadFirstPage)

                // WHEN
                appStateFlow.value = createAppState(content = postListContent)

                // THEN
                coVerify {
                    mockGetAllPosts.invoke(
                        GetPostParams(
                            sorting = postListContent.sortType,
                            searchTerm = postListContent.searchParameters.term,
                            tags = GetPostParams.Tags.Tagged(emptyList()),
                            offset = 0,
                            forceRefresh = false,
                        ),
                    )
                    mockAppStateRepository.runAction(SetPosts(mockResponse))
                }
            }

        @Test
        fun `WHEN PostDetailContent is emitted AND should load content is true THEN loadContent should be called`() =
            runTest {
                // GIVEN
                val postListContent = createPostListContent(shouldLoad = ShouldForceLoad)
                val postDetailContent = PostDetailContent(
                    post = mockk(),
                    previousContent = postListContent,
                )

                // WHEN
                appStateFlow.value = createAppState(content = postDetailContent)

                // THEN
                coVerify {
                    mockGetAllPosts.invoke(
                        GetPostParams(
                            sorting = postListContent.sortType,
                            searchTerm = postListContent.searchParameters.term,
                            tags = GetPostParams.Tags.Tagged(emptyList()),
                            offset = 0,
                            forceRefresh = true,
                        ),
                    )
                    mockAppStateRepository.runAction(SetPosts(mockResponse))
                }
            }
    }

    @Nested
    inner class LoadContentTest {

        @Test
        fun `GIVEN should load is Loaded WHEN loadContent is called THEN nothing else is called`() = runTest {
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
            verify { mockGetAllPosts wasNot Called }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class OffsetTests {

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is All THEN getAll should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) = runTest {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase

                // WHEN
                postListViewModel.loadContent(
                    createPostListContent(
                        category = All,
                        shouldLoad = shouldLoad,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            searchTerm = mockSearchTerm,
                            tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                            offset = expectedOffset,
                            forceRefresh = shouldLoad is ShouldForceLoad,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Public THEN getPublic should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) = runTest {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase

                // WHEN
                postListViewModel.loadContent(
                    createPostListContent(
                        category = Public,
                        shouldLoad = shouldLoad,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            searchTerm = mockSearchTerm,
                            tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                            visibility = PostVisibility.Public,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Unread THEN getUnread should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) = runTest {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase

                // WHEN
                postListViewModel.loadContent(
                    createPostListContent(
                        category = Unread,
                        shouldLoad = shouldLoad,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            searchTerm = mockSearchTerm,
                            tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                            readLater = true,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Private THEN getPrivate should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) = runTest {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase

                // WHEN
                postListViewModel.loadContent(
                    createPostListContent(
                        category = Private,
                        shouldLoad = shouldLoad,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            searchTerm = mockSearchTerm,
                            tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                            visibility = PostVisibility.Private,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Untagged THEN getUntagged should be called`(
                testCase: Pair<ShouldLoad, Int>,
            ) = runTest {
                // GIVEN
                val (shouldLoad, expectedOffset) = testCase

                // WHEN
                postListViewModel.loadContent(
                    createPostListContent(
                        category = Untagged,
                        shouldLoad = shouldLoad,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            searchTerm = mockSearchTerm,
                            tags = GetPostParams.Tags.Untagged,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            fun testCases(): List<Pair<ShouldLoad, Int>> = mutableListOf<Pair<ShouldLoad, Int>>().apply {
                add(ShouldForceLoad to 0)
                add(ShouldLoadFirstPage to 0)
                add(ShouldLoadNextPage(13) to 13)
            }
        }

        @Test
        fun `WHEN loadContent is called AND category is Recent THEN getRecent should be called`() = runTest {
            // WHEN
            postListViewModel.loadContent(
                createPostListContent(
                    category = Recent,
                    shouldLoad = ShouldLoadFirstPage,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                ),
            )

            // THEN
            coVerify {
                mockGetRecentPosts.invoke(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                    ),
                )
            }
        }
    }

    @Nested
    inner class CategoryTests {

        @Test
        fun `WHEN getAll is called THEN launchGetAll is called with the expected GetPostParams`() = runTest {
            val force = randomBoolean()

            postListViewModel.getAll(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset, force)

            coVerify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                        offset = mockOffset,
                        forceRefresh = force,
                    ),
                )
            }
        }

        @Test
        fun `GIVEN getRecentPosts will fail WHEN launchGetAll is called THEN repository won't run any actions`() =
            runTest {
                every { mockGetRecentPosts(any()) } returns flowOf(Failure(mockException))

                postListViewModel.getRecent(mockSortType, mockSearchTerm, SAMPLE_TAGS)

                coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
                assertThat(postListViewModel.error.first()).isEqualTo(mockException)
            }

        @Test
        fun `GIVEN getRecentPosts will succeed WHEN launchGetAll is called THEN repository will run SetPosts`() =
            runTest {
                postListViewModel.getRecent(mockSortType, mockSearchTerm, SAMPLE_TAGS)

                coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
                assertThat(postListViewModel.error.first()).isNull()
            }

        @Test
        fun `WHEN getPublic is called THEN launchGetAll is called with the expected GetPostParams`() = runTest {
            postListViewModel.getPublic(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset)

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                        visibility = PostVisibility.Public,
                        offset = mockOffset,
                    ),
                )
            }
        }

        @Test
        fun `WHEN getPrivate is called THEN launchGetAll is called with the expected GetPostParams`() = runTest {
            postListViewModel.getPrivate(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset)

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                        visibility = PostVisibility.Private,
                        offset = mockOffset,
                    ),
                )
            }
        }

        @Test
        fun `WHEN getUnread is called THEN launchGetAll is called with the expected GetPostParams`() = runTest {
            postListViewModel.getUnread(mockSortType, mockSearchTerm, SAMPLE_TAGS, mockOffset)

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                        readLater = true,
                        offset = mockOffset,
                    ),
                )
            }
        }

        @Test
        fun `WHEN getUntagged is called THEN launchGetAll is called with the expected GetPostParams`() = runTest {
            postListViewModel.getUntagged(mockSortType, mockSearchTerm, mockOffset)

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Untagged,
                        offset = mockOffset,
                    ),
                )
            }
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
                assertThat(postListViewModel.error.first()).isNull()
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
                assertThat(postListViewModel.error.first()).isNull()
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
