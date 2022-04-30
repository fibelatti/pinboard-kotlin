package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.MockDataProvider.mockTag1
import com.fibelatti.pinboard.MockDataProvider.mockTag2
import com.fibelatti.pinboard.MockDataProvider.mockTag3
import com.fibelatti.pinboard.MockDataProvider.mockTag4
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SearchActionHandlerTest {

    private val searchActionHandler = SearchActionHandler()

    @Nested
    inner class RefreshSearchTagsTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { searchActionHandler.runAction(RefreshSearchTags, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(),
                shouldLoadTags = false,
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(RefreshSearchTags, initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(),
                    shouldLoadTags = true,
                    previousContent = mockPreviousContent
                )
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
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                previousContent = mockPreviousContent
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
                        tags = listOf(mockTag1),
                    ),
                    availableTags = emptyList(),
                    allTags = emptyList(),
                    shouldLoadTags = true,
                    previousContent = mockPreviousContent,
                )
            )
        }
    }

    @Nested
    inner class SetSearchTagsTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(mockk<SetSearchTags>(), content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(
                    SetSearchTags(listOf(mockTag1, mockTag2, mockTag3)),
                    initialContent
                )
            }

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = mockUrlValid,
                        tags = listOf(mockTag1)
                    ),
                    availableTags = listOf(mockTag2, mockTag3),
                    allTags = listOf(mockTag1, mockTag2, mockTag3),
                    shouldLoadTags = false,
                    previousContent = mockPreviousContent
                )
            )
        }
    }

    @Nested
    inner class AddSearchTagTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(mockk<AddSearchTag>(), content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `GIVEN the tag is already included in the current SearchContent WHEN currentContent is SearchContent THEN same content is returned`() {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(AddSearchTag(mockTag1), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(initialContent)
        }

        @Test
        fun `GIVEN the max amount of tags has been reached SearchContent WHEN currentContent is SearchContent THEN same content is returned`() {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(
                    term = mockUrlValid,
                    tags = listOf(mockTag1, mockTag2, mockTag3)
                ),
                availableTags = listOf(mockTag4),
                allTags = listOf(mockTag1, mockTag2, mockTag3, mockTag4),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(AddSearchTag(mockTag4), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(initialContent)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(AddSearchTag(mockTag2), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = mockUrlValid,
                        tags = listOf(mockTag1, mockTag2)
                    ),
                    availableTags = listOf(mockTag3),
                    allTags = listOf(mockTag1, mockTag2, mockTag3),
                    previousContent = mockPreviousContent
                )
            )
        }
    }

    @Nested
    inner class RemoveSearchTagTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(mockk<RemoveSearchTag>(), content)
            }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mockk<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(RemoveSearchTag(mockTag1), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                SearchContent(
                    searchParameters = SearchParameters(
                        term = mockUrlValid, tags = emptyList()
                    ),
                    availableTags = listOf(mockTag1, mockTag2, mockTag3),
                    allTags = listOf(mockTag1, mockTag2, mockTag3),
                    previousContent = mockPreviousContent
                )
            )
        }
    }

    @Nested
    inner class SearchTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { searchActionHandler.runAction(mockk<Search>(), content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated PostListContent is returned`() {
            // GIVEN
            val mockPreviousContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(Search("new term"), initialContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(term = "new term", tags = listOf(mockTag1)),
                    shouldLoad = ShouldLoadFirstPage
                )
            )
        }

        @Test
        fun `WHEN currentContent is SearchContent AND category is not All THEN an updated PostListContent is returned`() = runTest {
            // GIVEN
            val mockPreviousContent = PostListContent(
                category = mockk(),
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(Search("new term"), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(term = "new term", tags = listOf(mockTag1)),
                    shouldLoad = ShouldLoadFirstPage
                )
            )
        }
    }

    @Nested
    inner class ClearSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mockk<PostDetailContent>()

            // WHEN
            val result = runBlocking { searchActionHandler.runAction(ClearSearch, content) }

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN an updated PostListContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                shouldLoad = Loaded
            )

            // WHEN
            val result = runBlocking { searchActionHandler.runAction(ClearSearch, initialContent) }

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage
                )
            )
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated PostListContent is returned`() {
            // GIVEN
            val previousContent = PostListContent(
                category = All,
                posts = null,
                showDescription = false,
                sortType = NewestFirst,
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                shouldLoad = Loaded
            )
            val searchContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = previousContent
            )

            // WHEN
            val result = runBlocking {
                searchActionHandler.runAction(ClearSearch, searchContent)
            }

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = null,
                    showDescription = false,
                    sortType = NewestFirst,
                    searchParameters = SearchParameters(),
                    shouldLoad = ShouldLoadFirstPage
                )
            )
        }
    }
}
