package com.fibelatti.pinboard.features.appstate

import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class PostListContentTest {

    private val nullContent = PostListContent(
        category = mockk(),
        posts = null,
        showDescription = false,
        sortType = mockk(),
        searchParameters = mockk(),
        shouldLoad = mockk(),
        isConnected = true,
    )

    private val nonNullContent = PostListContent(
        category = mockk(),
        posts = PostList(
            list = listOf(createPost()),
            totalCount = 42,
            canPaginate = true,
        ),
        showDescription = false,
        sortType = mockk(),
        searchParameters = mockk(),
        shouldLoad = mockk(),
        isConnected = true,
    )

    @Test
    fun `WHEN posts is null THEN totalCount should return 0`() {
        assertThat(nullContent.totalCount).isEqualTo(0)
    }

    @Test
    fun `WHEN posts is not null THEN totalCount should return its values`() {
        assertThat(nonNullContent.totalCount).isEqualTo(42)
    }

    @Test
    fun `WHEN posts is null THEN currentCount should return 0`() {
        assertThat(nullContent.currentCount).isEqualTo(0)
    }

    @Test
    fun `WHEN posts is not null THEN currentCount should return its values`() {
        assertThat(nonNullContent.currentCount).isEqualTo(1)
    }

    @Test
    fun `WHEN posts is null THEN currentList should return an emptyList`() {
        assertThat(nullContent.currentList).isEqualTo(emptyList<Post>())
    }

    @Test
    fun `WHEN posts is not null THEN currentList should return its values`() {
        assertThat(nonNullContent.currentList).isEqualTo(listOf(createPost()))
    }
}
