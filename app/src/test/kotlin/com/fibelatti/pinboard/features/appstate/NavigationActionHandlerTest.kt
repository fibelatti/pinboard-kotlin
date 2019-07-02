package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.isConnected
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
    private val mockConnectivityManager = mock<ConnectivityManager>()

    private val navigationActionHandler = NavigationActionHandler(
        mockResourceProvider,
        mockConnectivityManager
    )

    @Nested
    inner class NavigateBackTests {

        private val previousContent = PostList(
            category = All,
            title = mockTitle,
            posts = null,
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = true
        )

        @Test
        fun `WHEN currentContent is not ContentWithHistory THEN same content is returned`() {
            // GIVEN
            val content = mock<PostList>()

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostDetail THEN previousContent is returned`() {
            // GIVEN
            val mockPostDetail = mock<PostDetail>()
            given(mockPostDetail.previousContent).willReturn(previousContent)

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, mockPostDetail)

            // THEN
            result shouldBe previousContent
        }

        @Test
        fun `WHEN currentContent is SearchView THEN previousContent is returned`() {
            // GIVEN
            val mockSearchView = mock<SearchView>()
            given(mockSearchView.previousContent).willReturn(previousContent)

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, mockSearchView)

            // THEN
            result shouldBe previousContent
        }

        @Test
        fun `WHEN currentContent is AddPostView THEN previousContent is returned`() {
            // GIVEN
            val mockAddPostView = mock<AddPostView>()
            given(mockAddPostView.previousContent).willReturn(previousContent)

            // WHEN
            val result = navigationActionHandler.runAction(NavigateBack, mockAddPostView)

            // THEN
            result shouldBe previousContent
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class ViewCategoryTest {

        private val mockActiveNetworkInfo = mock<NetworkInfo>()

        @BeforeEach
        fun setup() {
            reset(mockConnectivityManager)
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN action is ViewCategory THEN a PostList is returned`(testCase: Triple<ViewCategory, Int, String>) {
            // GIVEN
            val (category, stringId, resolvedString) = testCase
            given(mockResourceProvider.getString(stringId))
                .willReturn(resolvedString)
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(false)

            // WHEN
            val result = navigationActionHandler.runAction(category, mock())

            // THEN
            result shouldBe PostList(
                category = category,
                title = resolvedString,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = false,
                isConnected = false
            )

            verify(mockConnectivityManager, times(2)).isConnected()
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
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewPost(createPost()), content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN PostDetail is returned`() {
            // GIVEN
            val initialContent = PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = true
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewPost(createPost()), initialContent)

            // THEN
            result shouldBe PostDetail(post = createPost(), previousContent = initialContent)
        }
    }

    @Nested
    inner class ViewSearchTests {

        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN SearchView is returned`() {
            // GIVEN
            val initialContent = PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = true
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewSearch, initialContent)

            // THEN
            result shouldBe SearchView(
                initialContent.searchParameters,
                shouldLoadTags = true,
                previousContent = initialContent
            )
        }
    }

    @Nested
    inner class AddPostTests {

        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN AddPostView is returned`() {
            // GIVEN
            val initialContent = PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = true
            )

            // WHEN
            val result = navigationActionHandler.runAction(AddPost, initialContent)

            // THEN
            result shouldBe AddPostView(previousContent = initialContent)
        }
    }

    @Nested
    inner class ViewTagsTests {

        private val mockActiveNetworkInfo = mock<NetworkInfo>()

        @Test
        fun `WHEN currentContent is not PostList THEN same content is returned`() {
            // GIVEN
            val content = mock<PostDetail>()

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, content)

            // THEN
            result shouldBe content
        }

        @Test
        fun `WHEN currentContent is PostList THEN TagList is returned`() {
            // GIVEN
            given(mockConnectivityManager.activeNetworkInfo)
                .willReturn(mockActiveNetworkInfo)
            given(mockActiveNetworkInfo.isConnected)
                .willReturn(false)

            val initialContent = PostList(
                category = All,
                title = mockTitle,
                posts = null,
                sortType = NewestFirst,
                searchParameters = SearchParameters(),
                shouldLoad = true
            )

            // WHEN
            val result = navigationActionHandler.runAction(ViewTags, initialContent)

            // THEN
            result shouldBe TagList(
                tags = emptyList(),
                shouldLoad = false,
                isConnected = false,
                previousContent = initialContent
            )

            verify(mockConnectivityManager, times(2)).isConnected()
        }
    }
}
