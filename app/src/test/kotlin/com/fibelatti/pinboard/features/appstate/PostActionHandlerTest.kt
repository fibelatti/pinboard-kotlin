package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PostActionHandlerTest {

    private val postActionHandler = PostActionHandler()

    private val initialContent = PostList(
        category = All,
        title = mockTitle,
        posts = emptyList(),
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = true
    )

    @Nested
    inner class RefreshTests {
        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(Refresh, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN updated content is returned`() {
            // WHEN
            val result = postActionHandler.runAction(Refresh, initialContent.copy(shouldLoad = false))

            // THEN
            result shouldBe initialContent.copy(shouldLoad = true)
        }
    }

    @Nested
    inner class SetPostsTests {
        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(SetPosts(listOf(createPost())), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN updated content is returned`() {
            // WHEN
            val result = postActionHandler.runAction(SetPosts(listOf(createPost())), initialContent)

            // THEN
            result shouldBe initialContent.copy(posts = listOf(createPost()), shouldLoad = false)
        }
    }

    @Nested
    inner class ToggleSortingTests {
        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `GIVEN sortType is NewestFirst WHEN currentContent is PostList THEN updated content is returned`() {
            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, initialContent.copy(sortType = NewestFirst))

            // THEN
            result shouldBe initialContent.copy(sortType = OldestFirst, shouldLoad = true)
        }

        @Test
        fun `GIVEN sortType is OldestFirst WHEN currentContent is PostList THEN updated content is returned`() {
            // WHEN
            val result = postActionHandler.runAction(ToggleSorting, initialContent.copy(sortType = OldestFirst))

            // THEN
            result shouldBe initialContent.copy(sortType = NewestFirst, shouldLoad = true)
        }
    }
}
