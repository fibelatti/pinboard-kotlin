package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.mock
import com.fibelatti.pinboard.InstantExecutorExtension
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.createTag
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.core.functional.SingleRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify

@ExtendWith(InstantExecutorExtension::class)
internal class AppStateDataSourceTest {

    private val mockResourceProvider = mock<ResourceProvider>()
    private val mockNavigationActionHandler = mock<NavigationActionHandler>()
    private val mockPostActionHandler = mock<PostActionHandler>()
    private val mockSearchActionHandler = mock<SearchActionHandler>()
    private val singleRunner = SingleRunner()
    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockActiveNetworkInfo = mock<NetworkInfo>()

    private lateinit var appStateDataSource: AppStateDataSource

    private val expectedInitialValue = PostList(
        category = All,
        title = "R.string.posts_title_all",
        posts = emptyList(),
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = true,
        isConnected = false
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        given(mockResourceProvider.getString(R.string.posts_title_all))
            .willReturn("R.string.posts_title_all")

        given(mockConnectivityManager.activeNetworkInfo)
            .willReturn(mockActiveNetworkInfo)
        given(mockActiveNetworkInfo.isConnected)
            .willReturn(false)

        appStateDataSource = AppStateDataSource(
            mockResourceProvider,
            mockNavigationActionHandler,
            mockPostActionHandler,
            mockSearchActionHandler,
            singleRunner,
            mockConnectivityManager
        )
    }

    @Test
    fun `AppStateDataSource initial value should be set`() {
        appStateDataSource.getContent().currentValueShouldBe(expectedInitialValue)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RunActionTests {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `WHEN runAction is called THEN expected action handler is called`(testCase: Pair<Action, ExpectedHandler>) {
            // GIVEN
            val (action, expectedHandler) = testCase

            // WHEN
            runBlocking { appStateDataSource.runAction(action) }

            // THEN
            when {
                expectedHandler == ExpectedHandler.NAVIGATION && action is NavigationAction -> {
                    verify(mockNavigationActionHandler).runAction(action, expectedInitialValue)
                }
                expectedHandler == ExpectedHandler.POST && action is PostAction -> {
                    verify(mockPostActionHandler).runAction(action, expectedInitialValue)
                }
                expectedHandler == ExpectedHandler.SEARCH && action is SearchAction -> {
                    verify(mockSearchActionHandler).runAction(action, expectedInitialValue)
                }
                else -> fail { "Unexpected Action received" }
            }
        }

        fun testCases(): List<Pair<Action, ExpectedHandler>> = listOf(
            NavigateBack to ExpectedHandler.NAVIGATION,
            All to ExpectedHandler.NAVIGATION,
            Recent to ExpectedHandler.NAVIGATION,
            Public to ExpectedHandler.NAVIGATION,
            Private to ExpectedHandler.NAVIGATION,
            Unread to ExpectedHandler.NAVIGATION,
            Untagged to ExpectedHandler.NAVIGATION,
            AllTags to ExpectedHandler.NAVIGATION,
            PostsForTag("") to ExpectedHandler.NAVIGATION,
            ViewPost(createPost()) to ExpectedHandler.NAVIGATION,
            ViewSearch to ExpectedHandler.NAVIGATION,
            AddPost to ExpectedHandler.NAVIGATION,

            SetPosts(listOf(createPost())) to ExpectedHandler.POST,
            ToggleSorting to ExpectedHandler.POST,

            SetSearchTags(listOf(createTag())) to ExpectedHandler.SEARCH,
            AddSearchTag(createTag()) to ExpectedHandler.SEARCH,
            RemoveSearchTag(createTag()) to ExpectedHandler.SEARCH,
            Search("term") to ExpectedHandler.SEARCH,
            ClearSearch to ExpectedHandler.SEARCH
        )
    }

    internal enum class ExpectedHandler {
        NAVIGATION, POST, SEARCH
    }
}
