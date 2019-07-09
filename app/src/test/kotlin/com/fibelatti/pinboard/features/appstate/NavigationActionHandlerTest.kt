package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.reset
import org.mockito.Mockito.times

internal class NavigationActionHandlerTest {

    private val mockResourceProvider = mock<ResourceProvider>()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private val navigationActionHandler = NavigationActionHandler(
        mockResourceProvider,
        mockConnectivityInfoProvider
    )

    @Nested
    inner class NavigateBackTests {

        private val previousContent = PostListContent(
            category = All,
            title = mockTitle,
            posts = null,
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage
        )

        @Test
        fun `WHEN currentContent is not ContentWithHistory THEN same content is returned`() {
            // GIVEN
            val content = mock<PostListContent>()

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetailContent THEN previousContent is returned`() {
            // GIVEN
            val mockPostDetail = mock<PostDetailContent>()
            given(mockPostDetail.previousContent).willReturn(previousContent)

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, mockPostDetail)

            // THEN
            result shouldBe previousContent
        }

        @Test
        fun `WHEN currentContent is SearchContent THEN previousContent is returned`() {
            // GIVEN
            val mockSearchView = mock<SearchContent>()
            given(mockSearchView.previousContent).willReturn(previousContent)

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, mockSearchView)

            // THEN
            result shouldBe previousContent
        }

        @Test
        fun `WHEN currentContent is AddPostContent THEN previousContent is returned`() {
            // GIVEN
            val mockAddPostContent = mock<AddPostContent>()
            given(mockAddPostContent.previousContent).willReturn(previousContent)

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, mockAddPostContent)

            // THEN
            result shouldBe previousContent
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ViewCategoryTest {

        @BeforeEach
        fun setup() {
            reset(mockConnectivityInfoProvider)
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN action is ViewCategory THEN a PostListContent is returned`(testCase: Triple<ViewCategory, Int, String>) {
            // GIVEN
            val (category, stringId, resolvedString) = testCase
            given(mockResourceProvider.getString(stringId))
                .willReturn(resolvedString)
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            // WHEN
            val result = navigationActionHandler.runAction(category, mock())

            // THEN
            result shouldBe PostListContent(
                category = category,
                title = resolvedString,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = Loaded,
                isConnected = false
            )

            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }

        fun testCases(): List<Triple<ViewCategory, Int, String>> =
            mutableListOf<Triple<ViewCategory, Int, String>>().apply {
                ViewCategory::class.sealedSubclasses.map { it.objectInstance as ViewCategory }.forEach { category ->
                    when (category) {
                        All -> add(Triple(category, R.string.posts_title_all, "R.string.posts_title_all"))
                        Recent -> add(Triple(category, R.string.posts_title_recent, "R.string.posts_title_recent"))
                        Public -> add(Triple(category, R.string.posts_title_public, "R.string.posts_title_public"))
                        Private -> add(Triple(category, R.string.posts_title_private, "R.string.posts_title_private"))
                        Unread -> add(Triple(category, R.string.posts_title_unread, "R.string.posts_title_unread"))
                        Untagged -> {
                            add(Triple(category, R.string.posts_title_untagged, "R.string.posts_title_untagged"))
                        }
                    }.let { }
                }
            }
    }

    @Nested
    inner class ViewPostTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewPost(createPost()), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN PostDetailContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewPost(createPost()), initialContent)

            // THEN
            result shouldBe PostDetailContent(post = createPost(), previousContent = initialContent)
        }
    }

    @Nested
    inner class ViewSearchTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN SearchContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, initialContent)

            // THEN
            result shouldBe SearchContent(
                initialContent.searchParameters,
                shouldLoadTags = true,
                previousContent = initialContent
            )
        }
    }

    @Nested
    inner class AddPostTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN AddPostContent is returned`() {
            // GIVEN
            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, initialContent)

            // THEN
            result shouldBe AddPostContent(previousContent = initialContent)
        }
    }

    @Nested
    inner class ViewTagsTests {

        @Test
        fun `WHEN currentContent is not PostListContent THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetailContent>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostListContent THEN TagListContent is returned`() {
            // GIVEN
            given(mockConnectivityInfoProvider.isConnected())
                .willReturn(false)

            val initialContent = PostListContent(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = ShouldLoadFirstPage
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, initialContent)

            // THEN
            result shouldBe TagListContent(
                tags = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = initialContent
            )

            verify(mockConnectivityInfoProvider, times(2)).isConnected()
        }
    }
}
