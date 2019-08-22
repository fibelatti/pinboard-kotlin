package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.features.posts.domain.model.Post
import org.junit.jupiter.api.Test

internal class PostListContentTest {

    private val nullContent = PostListContent(
        category = mock(),
        title = mockTitle,
        posts = null,
        showDescription = false,
        sortType = mock(),
        searchParameters = mock(),
        shouldLoad = mock(),
        isConnected = true
    )

    private val nonNullContent = PostListContent(
        category = mock(),
        title = mockTitle,
        posts = PostList(
            totalCount = 42,
            list = listOf(createPost()),
            diffUtil = mock(),
            alreadyDisplayed = true
        ),
        showDescription = false,
        sortType = mock(),
        searchParameters = mock(),
        shouldLoad = mock(),
        isConnected = true
    )

    @Test
    fun `WHEN posts is null THEN totalCount should return 0`() {
        nullContent.totalCount shouldBe 0
    }

    @Test
    fun `WHEN posts is not null THEN totalCount should return its values`() {
        nonNullContent.totalCount shouldBe 42
    }

    @Test
    fun `WHEN posts is null THEN currentCount should return 0`() {
        nullContent.currentCount shouldBe 0
    }

    @Test
    fun `WHEN posts is not null THEN currentCount should return its values`() {
        nonNullContent.currentCount shouldBe 1
    }

    @Test
    fun `WHEN posts is null THEN currentList should return an emptyList`() {
        nullContent.currentList shouldBe emptyList<Post>()
    }

    @Test
    fun `WHEN posts is not null THEN currentList should return its values`() {
        nonNullContent.currentList shouldBe listOf(createPost())
    }
}
