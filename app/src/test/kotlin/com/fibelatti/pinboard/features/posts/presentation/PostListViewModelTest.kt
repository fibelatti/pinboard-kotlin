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

@Suppress("UnusedFlow")
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
                            matchAll = postListContent.searchParameters.matchAll,
                            exactMatch = postListContent.searchParameters.exactMatch,
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
                            matchAll = postListContent.searchParameters.matchAll,
                            exactMatch = postListContent.searchParameters.exactMatch,
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

        @Test
        fun `WHEN getAllPosts fails THEN repository won't run any actions`() = runTest {
            every { mockGetAllPosts(any()) } returns flowOf(Failure(mockException))

            postListViewModel.loadContent(
                createPostListContent(
                    category = All,
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )

            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
            assertThat(postListViewModel.error.first()).isEqualTo(mockException)
        }

        @Test
        fun `WHEN getAllPosts succeeds and offset is 0 THEN repository will run SetPosts`() = runTest {
            // GIVEN
            every { mockGetAllPosts(GetPostParams(offset = 0)) } returns flowOf(Success(mockResponse))

            // WHEN
            postListViewModel.loadContent(
                createPostListContent(
                    category = All,
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )

            // THEN
            coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
            assertThat(postListViewModel.error.first()).isNull()
        }

        @Test
        fun `WHEN getAllPosts succeeds and offset is greater than 0 THEN repository will run SetNextPostPage`() =
            runTest {
                // GIVEN
                every { mockGetAllPosts(GetPostParams(offset = 1)) } returns flowOf(Success(mockResponse))

                // WHEN
                postListViewModel.loadContent(
                    createPostListContent(
                        category = All,
                        shouldLoad = ShouldLoadNextPage(offset = 1),
                    ),
                )

                // THEN
                coVerify { mockAppStateRepository.runAction(SetNextPostPage(mockResponse)) }
                assertThat(postListViewModel.error.first()).isNull()
            }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        inner class OffsetTests {

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is All THEN getAllPosts should be called`(
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
            fun `WHEN loadContent is called AND category is Public THEN getAllPosts should be called`(
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
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            visibility = PostVisibility.Public,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Unread THEN getAllPosts should be called`(
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
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            readLater = true,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Private THEN getAllPosts should be called`(
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
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
                            visibility = PostVisibility.Private,
                            offset = expectedOffset,
                        ),
                    )
                }
            }

            @ParameterizedTest
            @MethodSource("testCases")
            fun `WHEN loadContent is called AND category is Untagged THEN getAllPosts should be called`(
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
                    ),
                )

                // THEN
                verify {
                    mockGetAllPosts(
                        GetPostParams(
                            sorting = mockSortType,
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
    }

    @Nested
    inner class CategoryTests {

        @Test
        fun `WHEN category is All THEN getAllPosts is called with the expected params`() = runTest {
            postListViewModel.loadContent(
                createPostListContent(
                    category = All,
                    shouldLoad = ShouldLoadFirstPage,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(term = mockSearchTerm, tags = SAMPLE_TAGS),
                ),
            )

            coVerify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        searchTerm = mockSearchTerm,
                        tags = GetPostParams.Tags.Tagged(SAMPLE_TAGS),
                        offset = 0,
                        forceRefresh = false,
                    ),
                )
            }
        }

        @Test
        fun `WHEN category is Recent THEN getRecentPosts should be called`() = runTest {
            // WHEN
            postListViewModel.loadContent(
                createPostListContent(
                    category = Recent,
                    sortType = mockSortType,
                ),
            )

            // THEN
            coVerify {
                mockGetRecentPosts.invoke(mockSortType)
            }
        }

        @Test
        fun `WHEN getRecentPosts fails THEN repository won't run any actions`() = runTest {
            every { mockGetRecentPosts(any()) } returns flowOf(Failure(mockException))

            postListViewModel.loadContent(
                createPostListContent(
                    category = Recent,
                    sortType = mockSortType,
                ),
            )

            coVerify(exactly = 0) { mockAppStateRepository.runAction(any()) }
            assertThat(postListViewModel.error.first()).isEqualTo(mockException)
        }

        @Test
        fun `WHEN getRecentPosts succeeds THEN repository will run SetPosts`() = runTest {
            postListViewModel.loadContent(
                createPostListContent(
                    category = Recent,
                    sortType = mockSortType,
                ),
            )

            coVerify { mockAppStateRepository.runAction(SetPosts(mockResponse)) }
            assertThat(postListViewModel.error.first()).isNull()
        }

        @Test
        fun `WHEN category is Public THEN getRecentPosts should be called`() = runTest {
            postListViewModel.loadContent(
                createPostListContent(
                    category = Public,
                    sortType = mockSortType,
                ),
            )

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        visibility = PostVisibility.Public,
                    ),
                )
            }
        }

        @Test
        fun `WHEN category is Private THEN getRecentPosts should be called`() = runTest {
            postListViewModel.loadContent(
                createPostListContent(
                    category = Private,
                    sortType = mockSortType,
                ),
            )

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        visibility = PostVisibility.Private,
                    ),
                )
            }
        }

        @Test
        fun `WHEN category is Unread THEN getRecentPosts should be called`() = runTest {
            postListViewModel.loadContent(
                createPostListContent(
                    category = Unread,
                    sortType = mockSortType,
                ),
            )

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        readLater = true,
                    ),
                )
            }
        }

        @Test
        fun `WHEN category is Untagged THEN getRecentPosts should be called`() = runTest {
            postListViewModel.loadContent(
                createPostListContent(
                    category = Untagged,
                    sortType = mockSortType,
                ),
            )

            verify {
                mockGetAllPosts(
                    GetPostParams(
                        sorting = mockSortType,
                        tags = GetPostParams.Tags.Untagged,
                    ),
                )
            }
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
