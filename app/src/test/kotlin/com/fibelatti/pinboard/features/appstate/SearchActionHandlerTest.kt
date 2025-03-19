package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_1
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_2
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_3
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_4
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_VALID
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SearchActionHandlerTest {

    private val mockSortType = mockk<SortType>()
    private val mockGetPreferredSortType = mockk<GetPreferredSortType> {
        every { this@mockk.invoke() } returns mockSortType
    }

    private val searchActionHandler = SearchActionHandler(
        getPreferredSortType = mockGetPreferredSortType,
    )

    @Nested
    inner class RefreshSearchTagsTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(RefreshSearchTags, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(),
                shouldLoadTags = false,
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(RefreshSearchTags, initialContent)

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(),
                    shouldLoadTags = true,
                    previousContent = mockPreviousContent,
                ),
            )
        }
    }

    @Nested
    inner class SetTermTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<SetTerm>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(
                SetTerm("updated-term"),
                initialContent,
            )

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = "updated-term",
                        tags = listOf(SAMPLE_TAG_1),
                    ),
                    availableTags = emptyList(),
                    allTags = emptyList(),
                    shouldLoadTags = true,
                    previousContent = mockPreviousContent,
                ),
            )
        }
    }

    @Nested
    inner class SetSearchTagsTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<SetSearchTags>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(
                SetSearchTags(listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3)),
                initialContent,
            )

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = SAMPLE_URL_VALID,
                        tags = listOf(SAMPLE_TAG_1),
                    ),
                    availableTags = listOf(SAMPLE_TAG_2, SAMPLE_TAG_3),
                    allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                    shouldLoadTags = false,
                    previousContent = mockPreviousContent,
                ),
            )
        }
    }

    @Nested
    inner class AddSearchTagTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<AddSearchTag>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `GIVEN the tag is already included in the current SearchContent WHEN currentContent is SearchContent THEN same content is returned`() =
            runTest {
                // GIVEN
                val mockPreviousContent = mockk<PostListContent>()
                val initialContent = SearchContent(
                    searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                    availableTags = listOf(SAMPLE_TAG_2, SAMPLE_TAG_3),
                    allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                    previousContent = mockPreviousContent,
                )

                // WHEN
                val result = searchActionHandler.runAction(AddSearchTag(SAMPLE_TAG_1), initialContent)

                // THEN
                assertThat(result).isEqualTo(initialContent)
            }

        @Test
        fun `GIVEN the max amount of tags has been reached SearchContent WHEN currentContent is SearchContent THEN same content is returned`() =
            runTest {
                // GIVEN
                val mockPreviousContent = mockk<PostListContent>()
                val initialContent = SearchContent(
                    searchParameters = SearchParameters(
                        term = SAMPLE_URL_VALID,
                        tags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                    ),
                    availableTags = listOf(SAMPLE_TAG_4),
                    allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3, SAMPLE_TAG_4),
                    previousContent = mockPreviousContent,
                )

                // WHEN
                val result = searchActionHandler.runAction(AddSearchTag(SAMPLE_TAG_4), initialContent)

                // THEN
                assertThat(result).isEqualTo(initialContent)
            }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                availableTags = listOf(SAMPLE_TAG_2, SAMPLE_TAG_3),
                allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(AddSearchTag(SAMPLE_TAG_2), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = SAMPLE_URL_VALID,
                        tags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2),
                    ),
                    availableTags = listOf(SAMPLE_TAG_3),
                    allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                    previousContent = mockPreviousContent,
                ),
            )
        }
    }

    @Nested
    inner class RemoveSearchTagTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<RemoveSearchTag>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                availableTags = listOf(SAMPLE_TAG_2, SAMPLE_TAG_3),
                allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(RemoveSearchTag(SAMPLE_TAG_1), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = SAMPLE_URL_VALID,
                        tags = emptyList(),
                    ),
                    availableTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                    allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                    previousContent = mockPreviousContent,
                ),
            )
        }
    }

    @Nested
    inner class SetResultSizeTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<SetResultSize>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(SetResultSize(13), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                    queryResultSize = 13,
                    previousContent = mockPreviousContent,
                ),
            )
        }
    }

    @Nested
    inner class SearchTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<Search>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated PostListContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = mockSortType,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage,
            )
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                previousContent = mockPreviousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(Search, initialContent)

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is SearchContent AND category is not All THEN an updated PostListContent is returned`() =
            runTest {
                // GIVEN
                val mockPreviousContent = PostListContent(
                    category = mockk(),
                    posts = null,
                    showDescription = false,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                )
                val initialContent = SearchContent(
                    searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                    previousContent = mockPreviousContent,
                )

                // WHEN
                val result = searchActionHandler.runAction(Search, initialContent)

                // THEN
                assertThat(result).isEqualTo(
                    PostListContent(
                        category = All,
                        posts = null,
                        showDescription = false,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                )
            }
    }

    @Nested
    inner class ClearSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<ExternalContent>()

            // WHEN
            val result = searchActionHandler.runAction(ClearSearch, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN an updated PostListContent is returned`() = runTest {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = mockSortType,
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                shouldLoad = Loaded,
            )

            // WHEN
            val result = searchActionHandler.runAction(ClearSearch, initialContent)

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN an updated PostDetailContent is returned`() = runTest {
            // GIVEN
            val initialContent = PostDetailContent(
                post = mockk(),
                previousContent = PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                    shouldLoad = Loaded,
                ),
            )

            // WHEN
            val result = searchActionHandler.runAction(ClearSearch, initialContent)

            // THEN
            assertThat(result).isEqualTo(
                initialContent.copy(
                    previousContent = PostListContent(
                        category = All,
                        posts = null,
                        showDescription = false,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(),
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                ),
            )
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated PostListContent is returned`() = runTest {
            // GIVEN
            val previousContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = mockSortType,
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                shouldLoad = Loaded,
            )
            val searchContent = SearchContent(
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                availableTags = listOf(SAMPLE_TAG_2, SAMPLE_TAG_3),
                allTags = listOf(SAMPLE_TAG_1, SAMPLE_TAG_2, SAMPLE_TAG_3),
                previousContent = previousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(ClearSearch, searchContent)

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )
        }
    }

    @Nested
    inner class ViewSavedFilterTests {

        @Test
        fun `WHEN currentContent is not SavedFiltersContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mockk<ViewSavedFilter>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SavedFiltersContent THEN an updated PostListContent is returned`() = runTest {
            // GIVEN
            val previousContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = mockSortType,
                searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                shouldLoad = ShouldLoadFirstPage,
            )
            val initialContent = SavedFiltersContent(
                previousContent = previousContent,
            )

            // WHEN
            val result = searchActionHandler.runAction(
                ViewSavedFilter(savedFilter = SavedFilter(searchTerm = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1))),
                initialContent,
            )

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(term = SAMPLE_URL_VALID, tags = listOf(SAMPLE_TAG_1)),
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )
        }
    }
}
