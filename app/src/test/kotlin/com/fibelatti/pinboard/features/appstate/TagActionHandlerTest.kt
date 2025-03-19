package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.MockDataProvider.createTag
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TagActionHandlerTest {

    private val mockUserRepository = mockk<UserRepository>()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private val mockSortType = mockk<SortType>()
    private val mockGetPreferredSortType = mockk<GetPreferredSortType> {
        every { this@mockk.invoke() } returns mockSortType
    }

    private val tagActionHandler = TagActionHandler(
        userRepository = mockUserRepository,
        connectivityInfoProvider = mockConnectivityInfoProvider,
        getPreferredSortType = mockGetPreferredSortType,
    )

    val mockPreviousContent = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
        sortType = mockSortType,
        searchParameters = SearchParameters(),
        shouldLoad = Loaded,
    )
    private val initialContent = TagListContent(
        tags = emptyList(),
        shouldLoad = true,
        isConnected = true,
        previousContent = mockPreviousContent,
    )

    @Nested
    inner class RefreshTagsTests {

        @Test
        fun `WHEN currentContent is not TagListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = tagActionHandler.runAction(RefreshTags, content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is TagListContent THEN updated content is returned`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns false

            // WHEN
            val result = tagActionHandler.runAction(RefreshTags, initialContent)

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = emptyList(),
                    shouldLoad = false,
                    isConnected = false,
                    previousContent = mockPreviousContent,
                ),
            )
            verify(exactly = 2) { mockConnectivityInfoProvider.isConnected() }
        }
    }

    @Nested
    inner class SetTagsTests {

        @Test
        fun `WHEN currentContent is not TagListContent THEN same content is returned`() = runTest {
            // GIVEN
            val content = mockk<PostListContent>()

            // WHEN
            val result = tagActionHandler.runAction(mockk<SetTags>(), content)

            // THEN
            assertThat(result).isEqualTo(content)
        }

        @Test
        fun `WHEN currentContent is TagListContent THEN updated content is returned`() = runTest {
            // WHEN
            val result = tagActionHandler.runAction(SetTags(listOf(createTag())), initialContent)

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = listOf(createTag()),
                    shouldLoad = false,
                    isConnected = true,
                    previousContent = mockPreviousContent,
                ),
            )
        }

        @Test
        fun `WHEN shouldUpdatePosts is true THEN previousContent is also updated`() = runTest {
            // GIVEN
            coJustRun { mockUserRepository.lastUpdate = "" }

            // WHEN
            val result = tagActionHandler.runAction(
                SetTags(listOf(createTag()), shouldReloadPosts = true),
                initialContent,
            )

            // THEN
            assertThat(result).isEqualTo(
                TagListContent(
                    tags = listOf(createTag()),
                    shouldLoad = false,
                    isConnected = true,
                    previousContent = mockPreviousContent.copy(
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                ),
            )
            verify {
                mockUserRepository.lastUpdate = ""
            }
        }
    }

    @Nested
    inner class PostsForTagTests {

        @Test
        fun `WHEN postsForTag is called THEN PostListContent is returned and search parameters contains the tag`() =
            runTest {
                // GIVEN
                every { mockConnectivityInfoProvider.isConnected() } returns true

                val randomBoolean = randomBoolean()
                every { mockUserRepository.showDescriptionInLists } returns randomBoolean

                // WHEN
                val result = tagActionHandler.runAction(PostsForTag(createTag()), initialContent)

                // THEN
                assertThat(result).isEqualTo(
                    PostListContent(
                        category = All,
                        posts = null,
                        showDescription = randomBoolean,
                        sortType = mockSortType,
                        searchParameters = SearchParameters(tags = listOf(createTag())),
                        shouldLoad = ShouldLoadFirstPage,
                        isConnected = true,
                    ),
                )
            }

        @Test
        fun `WHEN current is PostListContent THEN PostListContent is returned and posts is not null`() = runTest {
            // GIVEN
            every { mockConnectivityInfoProvider.isConnected() } returns true

            val randomBoolean = randomBoolean()
            every { mockUserRepository.showDescriptionInLists } returns randomBoolean

            val postList = mockk<PostList>()
            val current = mockk<PostListContent> {
                every { posts } returns postList
            }

            // WHEN
            val result = tagActionHandler.runAction(PostsForTag(createTag()), current)

            // THEN
            assertThat(result).isEqualTo(
                PostListContent(
                    category = All,
                    posts = postList,
                    showDescription = randomBoolean,
                    sortType = mockSortType,
                    searchParameters = SearchParameters(tags = listOf(createTag())),
                    shouldLoad = ShouldLoadFirstPage,
                    isConnected = true,
                ),
            )
        }
    }
}
