package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.mockTag1
import com.fibelatti.pinboard.MockDataProvider.mockTag2
import com.fibelatti.pinboard.MockDataProvider.mockTag3
import com.fibelatti.pinboard.MockDataProvider.mockTag4
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SearchActionHandlerTest {

    private val searchActionHandler = SearchActionHandler()

    @Nested
    inner class RefreshSearchTagsTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(RefreshSearchTags, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mock<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(),
                shouldLoadTags = false,
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(RefreshSearchTags, initialContent)

            // THEN
            result shouldBe SearchContent(
                searchParameters = SearchParameters(),
                shouldLoadTags = true,
                previousContent = mockPreviousContent
            )
        }
    }

    @Nested
    inner class SetSearchTagsTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mock<SetSearchTags>(), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mock<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(
                SetSearchTags(listOf(mockTag1, mockTag2, mockTag3)),
                initialContent
            )

            // THEN
            result shouldBe SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                shouldLoadTags = false,
                previousContent = mockPreviousContent
            )
        }
    }

    @Nested
    inner class AddSearchTagTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mock<AddSearchTag>(), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN the tag is already included in the current SearchContent WHEN currentContent is SearchContent THEN same content is returned`() {
            // GIVEN
            val mockPreviousContent = mock<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(AddSearchTag(mockTag1), initialContent)

            // THEN
            result shouldBe initialContent
        }

        @Test
        fun `GIVEN the max amount of tags has been reached SearchContent WHEN currentContent is SearchContent THEN same content is returned`() {
            // GIVEN
            val mockPreviousContent = mock<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1, mockTag2, mockTag3)),
                availableTags = listOf(mockTag4),
                allTags = listOf(mockTag1, mockTag2, mockTag3, mockTag4),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(AddSearchTag(mockTag4), initialContent)

            // THEN
            result shouldBe initialContent
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mock<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(AddSearchTag(mockTag2), initialContent)

            // THEN
            result shouldBe SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1, mockTag2)),
                availableTags = listOf(mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )
        }
    }

    @Nested
    inner class RemoveSearchTagTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mock<RemoveSearchTag>(), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated SearchContent is returned`() {
            // GIVEN
            val mockPreviousContent = mock<PostListContent>()
            val initialContent = SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                availableTags = listOf(mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )

            // WHEN
            val result = searchActionHandler.runAction(RemoveSearchTag(mockTag1), initialContent)

            // THEN
            result shouldBe SearchContent(
                searchParameters = SearchParameters(term = mockUrlValid, tags = emptyList()),
                availableTags = listOf(mockTag1, mockTag2, mockTag3),
                allTags = listOf(mockTag1, mockTag2, mockTag3),
                previousContent = mockPreviousContent
            )
        }
    }

    @Nested
    inner class SearchTests {

        @Test
        fun `WHEN currentContent is not SearchContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(mock<Search>(), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN an updated PostListContent is returned`() {
            // GIVEN
            val mockPreviousContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
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
            result shouldBe PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(term = "new term", tags = listOf(mockTag1)),
                shouldLoad = ShouldLoadFirstPage
            )
        }
    }

    @Nested
    inner class ClearSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = searchActionHandler.runAction(ClearSearch, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN an updated PostListContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(term = mockUrlValid, tags = listOf(mockTag1)),
                shouldLoad = Loaded
            )

            // WHEN
            val result = searchActionHandler.runAction(ClearSearch, initialContent)

            // THEN
            result shouldBe PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )
        }
    }
}
