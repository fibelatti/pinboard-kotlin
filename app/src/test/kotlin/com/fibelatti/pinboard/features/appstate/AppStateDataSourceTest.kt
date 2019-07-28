package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.SingleRunner
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.pinboard.InstantExecutorExtension
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
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
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@ExtendWith(InstantExecutorExtension::class)
internal class AppStateDataSourceTest {

    private val mockResourceProvider = mock<ResourceProvider>()
    private val mockNavigationActionHandler = mock<NavigationActionHandler>()
    private val mockPostActionHandler = mock<PostActionHandler>()
    private val mockSearchActionHandler = mock<SearchActionHandler>()
    private val mockTagActionHandler = mock<TagActionHandler>()
    private val mockNoteActionHandler = mock<NoteActionHandler>()
    private val singleRunner = SingleRunner()
    private val mockConnectivityInfoProvider = mock<ConnectivityInfoProvider>()

    private lateinit var appStateDataSource: AppStateDataSource

    private val expectedInitialValue = PostListContent(
        category = All,
        title = "R.string.posts_title_all",
        posts = null,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
        isConnected = false
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        given(mockResourceProvider.getString(R.string.posts_title_all))
            .willReturn("R.string.posts_title_all")

        given(mockConnectivityInfoProvider.isConnected())
            .willReturn(false)

        appStateDataSource = spy(
            AppStateDataSource(
                mockResourceProvider,
                mockNavigationActionHandler,
                mockPostActionHandler,
                mockSearchActionHandler,
                mockTagActionHandler,
                mockNoteActionHandler,
                singleRunner,
                mockConnectivityInfoProvider
            )
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
            runBlocking {
                // GIVEN
                given(mockNavigationActionHandler.runAction(safeAny(), safeAny())).willReturn(expectedInitialValue)
                given(mockPostActionHandler.runAction(safeAny(), safeAny())).willReturn(expectedInitialValue)
                given(mockSearchActionHandler.runAction(safeAny(), safeAny())).willReturn(expectedInitialValue)
                given(mockTagActionHandler.runAction(safeAny(), safeAny())).willReturn(expectedInitialValue)
                given(mockNoteActionHandler.runAction(safeAny(), safeAny())).willReturn(expectedInitialValue)

                val (action, expectedHandler) = testCase

                // WHEN
                appStateDataSource.runAction(action)

                // THEN
                when (expectedHandler) {
                    ExpectedHandler.NAVIGATION -> {
                        if (action is NavigationAction) {
                            verify(mockNavigationActionHandler).runAction(action, expectedInitialValue)
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.POST -> {
                        if (action is PostAction) {
                            verify(mockPostActionHandler).runAction(action, expectedInitialValue)
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.SEARCH -> {
                        if (action is SearchAction) {
                            verify(mockSearchActionHandler).runAction(action, expectedInitialValue)
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.TAG -> {
                        if (action is TagAction) {
                            verify(mockTagActionHandler).runAction(action, expectedInitialValue)
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.NOTE -> {
                        if (action is NoteAction) {
                            verify(mockNoteActionHandler).runAction(action, expectedInitialValue)
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                }.let { } // to make it exhaustive
            }
        }

        fun testCases(): List<Pair<Action, ExpectedHandler>> =
            mutableListOf<Pair<Action, ExpectedHandler>>().apply {
                Action::class.allSealedSubclasses
                    .map { it.objectInstance ?: Mockito.mock(it.javaObjectType) }
                    .forEach { action ->
                        when (action) {
                            // Navigation
                            NavigateBack -> add(NavigateBack to ExpectedHandler.NAVIGATION)
                            All -> add(All to ExpectedHandler.NAVIGATION)
                            Recent -> add(Recent to ExpectedHandler.NAVIGATION)
                            Public -> add(Public to ExpectedHandler.NAVIGATION)
                            Private -> add(Private to ExpectedHandler.NAVIGATION)
                            Unread -> add(Unread to ExpectedHandler.NAVIGATION)
                            Untagged -> add(Untagged to ExpectedHandler.NAVIGATION)
                            is ViewPost -> add(mock<ViewPost>() to ExpectedHandler.NAVIGATION)
                            ViewSearch -> add(ViewSearch to ExpectedHandler.NAVIGATION)
                            AddPost -> add(AddPost to ExpectedHandler.NAVIGATION)
                            ViewTags -> add(ViewTags to ExpectedHandler.NAVIGATION)
                            ViewNotes -> add(ViewNotes to ExpectedHandler.NAVIGATION)
                            is ViewNote -> add(mock<ViewNote>() to ExpectedHandler.NAVIGATION)
                            ViewPreferences -> add(ViewPreferences to ExpectedHandler.NAVIGATION)

                            // Post
                            Refresh -> add(Refresh to ExpectedHandler.POST)
                            is SetPosts -> add(mock<SetPosts>() to ExpectedHandler.POST)
                            GetNextPostPage -> add(GetNextPostPage to ExpectedHandler.POST)
                            is SetNextPostPage -> add(mock<SetNextPostPage>() to ExpectedHandler.POST)
                            PostsDisplayed -> add(PostsDisplayed to ExpectedHandler.POST)
                            ToggleSorting -> add(ToggleSorting to ExpectedHandler.POST)
                            is EditPost -> add(mock<EditPost>() to ExpectedHandler.POST)
                            is PostSaved -> add(mock<PostSaved>() to ExpectedHandler.POST)
                            PostDeleted -> add(PostDeleted to ExpectedHandler.POST)

                            // Search
                            RefreshSearchTags -> add(RefreshSearchTags to ExpectedHandler.SEARCH)
                            is SetSearchTags -> add(mock<SetSearchTags>() to ExpectedHandler.SEARCH)
                            is AddSearchTag -> add(mock<AddSearchTag>() to ExpectedHandler.SEARCH)
                            is RemoveSearchTag -> add(mock<RemoveSearchTag>() to ExpectedHandler.SEARCH)
                            is Search -> add(mock<Search>() to ExpectedHandler.SEARCH)
                            ClearSearch -> add(ClearSearch to ExpectedHandler.SEARCH)

                            // Tag
                            RefreshTags -> add(RefreshTags to ExpectedHandler.TAG)
                            is SetTags -> add(mock<SetTags>() to ExpectedHandler.TAG)
                            is PostsForTag -> add(mock<PostsForTag>() to ExpectedHandler.TAG)

                            // Notes
                            RefreshNotes -> add(RefreshNotes to ExpectedHandler.NOTE)
                            is SetNotes -> add(mock<SetNotes>() to ExpectedHandler.NOTE)
                            is SetNote -> add(mock<SetNote>() to ExpectedHandler.NOTE)
                        }.let { } // to make it exhaustive
                    }
            }
    }

    @Nested
    inner class DuplicateContentTests {

        @Test
        fun `WHEN NavigationActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mock<NavigationAction>()
            givenSuspend { mockNavigationActionHandler.runAction(mockAction, expectedInitialValue) }
                .willReturn(expectedInitialValue)

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            verify(appStateDataSource, never()).updateContent(safeAny())
        }

        @Test
        fun `WHEN PostActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mock<PostAction>()
            givenSuspend { mockPostActionHandler.runAction(mockAction, expectedInitialValue) }
                .willReturn(expectedInitialValue)

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            verify(appStateDataSource, never()).updateContent(safeAny())
        }

        @Test
        fun `WHEN SearchActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mock<SearchAction>()
            givenSuspend { mockSearchActionHandler.runAction(mockAction, expectedInitialValue) }
                .willReturn(expectedInitialValue)

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            verify(appStateDataSource, never()).updateContent(safeAny())
        }

        @Test
        fun `WHEN TagActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mock<TagAction>()
            givenSuspend { mockTagActionHandler.runAction(mockAction, expectedInitialValue) }
                .willReturn(expectedInitialValue)

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            verify(appStateDataSource, never()).updateContent(safeAny())
        }
    }

    internal enum class ExpectedHandler {
        NAVIGATION, POST, SEARCH, TAG, NOTE
    }
}
