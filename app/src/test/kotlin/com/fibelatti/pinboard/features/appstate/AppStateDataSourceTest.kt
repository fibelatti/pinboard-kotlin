package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.SingleRunner
import com.fibelatti.pinboard.InstantExecutorExtension
import com.fibelatti.pinboard.allSealedSubclasses
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
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

@ExtendWith(InstantExecutorExtension::class)
internal class AppStateDataSourceTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private val mockNavigationActionHandler = mockk<NavigationActionHandler>()
    private val mockPostActionHandler = mockk<PostActionHandler>()
    private val mockSearchActionHandler = mockk<SearchActionHandler>()
    private val mockTagActionHandler = mockk<TagActionHandler>()
    private val mockNoteActionHandler = mockk<NoteActionHandler>()
    private val mockPopularActionHandler = mockk<PopularActionHandler>()
    private val singleRunner = SingleRunner()
    private val mockConnectivityInfoProvider = mockk<ConnectivityInfoProvider>()

    private lateinit var appStateDataSource: AppStateDataSource

    private val expectedInitialValue = PostListContent(
        category = All,
        posts = null,
        showDescription = false,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
        isConnected = false
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)

        every { mockConnectivityInfoProvider.isConnected() } returns false

        appStateDataSource = spyk(
            AppStateDataSource(
                mockUserRepository,
                mockNavigationActionHandler,
                mockPostActionHandler,
                mockSearchActionHandler,
                mockTagActionHandler,
                mockNoteActionHandler,
                mockPopularActionHandler,
                singleRunner,
                mockConnectivityInfoProvider
            )
        )
    }

    @Test
    fun `AppStateDataSource initial value should be set`() {
        appStateDataSource.getContent().currentValueShouldBe(expectedInitialValue)
    }

    @Test
    fun `reset should set currentContent to the initial value`() {
        // GIVEN
        coEvery { appStateDataSource.getInitialContent() } returns expectedInitialValue

        // WHEN
        appStateDataSource.reset()

        // THEN
        verify { appStateDataSource.getInitialContent() }
        verify { appStateDataSource.updateContent(expectedInitialValue) }
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
                coEvery {
                    mockNavigationActionHandler.runAction(
                        any(),
                        any()
                    )
                } returns expectedInitialValue
                coEvery {
                    mockPostActionHandler.runAction(
                        any(),
                        any()
                    )
                } returns expectedInitialValue
                coEvery {
                    mockSearchActionHandler.runAction(
                        any(),
                        any()
                    )
                } returns expectedInitialValue
                coEvery {
                    mockTagActionHandler.runAction(
                        any(),
                        any()
                    )
                } returns expectedInitialValue
                coEvery {
                    mockNoteActionHandler.runAction(
                        any(),
                        any()
                    )
                } returns expectedInitialValue
                coEvery {
                    mockPopularActionHandler.runAction(
                        any(),
                        any()
                    )
                } returns expectedInitialValue

                val (action, expectedHandler) = testCase

                // WHEN
                appStateDataSource.runAction(action)

                // THEN
                when (expectedHandler) {
                    ExpectedHandler.NAVIGATION -> {
                        if (action is NavigationAction) {
                            coVerify {
                                mockNavigationActionHandler.runAction(
                                    action,
                                    expectedInitialValue
                                )
                            }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.POST -> {
                        if (action is PostAction) {
                            coVerify {
                                mockPostActionHandler.runAction(
                                    action,
                                    expectedInitialValue
                                )
                            }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.SEARCH -> {
                        if (action is SearchAction) {
                            coVerify {
                                mockSearchActionHandler.runAction(
                                    action,
                                    expectedInitialValue
                                )
                            }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.TAG -> {
                        if (action is TagAction) {
                            coVerify {
                                mockTagActionHandler.runAction(
                                    action,
                                    expectedInitialValue
                                )
                            }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.NOTE -> {
                        if (action is NoteAction) {
                            coVerify {
                                mockNoteActionHandler.runAction(
                                    action,
                                    expectedInitialValue
                                )
                            }
                        } else {
                            fail { "Unexpected Action received" }
                        }
                    }
                    ExpectedHandler.POPULAR -> {
                        if (action is PopularAction) {
                            coVerify {
                                mockPopularActionHandler.runAction(
                                    action,
                                    expectedInitialValue
                                )
                            }
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
                    .map { it.objectInstance ?: mockkClass(it) }
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
                            is ViewPost -> add(mockk<ViewPost>() to ExpectedHandler.NAVIGATION)
                            ViewSearch -> add(ViewSearch to ExpectedHandler.NAVIGATION)
                            AddPost -> add(AddPost to ExpectedHandler.NAVIGATION)
                            ViewTags -> add(ViewTags to ExpectedHandler.NAVIGATION)
                            ViewNotes -> add(ViewNotes to ExpectedHandler.NAVIGATION)
                            is ViewNote -> add(mockk<ViewNote>() to ExpectedHandler.NAVIGATION)
                            ViewPopular -> add(ViewPopular to ExpectedHandler.NAVIGATION)
                            ViewPreferences -> add(ViewPreferences to ExpectedHandler.NAVIGATION)

                            // Post
                            is Refresh -> add(mockk<Refresh>() to ExpectedHandler.POST)
                            is SetPosts -> add(mockk<SetPosts>() to ExpectedHandler.POST)
                            GetNextPostPage -> add(GetNextPostPage to ExpectedHandler.POST)
                            is SetNextPostPage -> add(mockk<SetNextPostPage>() to ExpectedHandler.POST)
                            PostsDisplayed -> add(PostsDisplayed to ExpectedHandler.POST)
                            ToggleSorting -> add(ToggleSorting to ExpectedHandler.POST)
                            is EditPost -> add(mockk<EditPost>() to ExpectedHandler.POST)
                            is EditPostFromShare -> add(mockk<EditPostFromShare>() to ExpectedHandler.POST)
                            is PostSaved -> add(mockk<PostSaved>() to ExpectedHandler.POST)
                            PostDeleted -> add(PostDeleted to ExpectedHandler.POST)

                            // Search
                            RefreshSearchTags -> add(RefreshSearchTags to ExpectedHandler.SEARCH)
                            is SetSearchTags -> add(mockk<SetSearchTags>() to ExpectedHandler.SEARCH)
                            is AddSearchTag -> add(mockk<AddSearchTag>() to ExpectedHandler.SEARCH)
                            is RemoveSearchTag -> add(mockk<RemoveSearchTag>() to ExpectedHandler.SEARCH)
                            is Search -> add(mockk<Search>() to ExpectedHandler.SEARCH)
                            ClearSearch -> add(ClearSearch to ExpectedHandler.SEARCH)

                            // Tag
                            RefreshTags -> add(RefreshTags to ExpectedHandler.TAG)
                            is SetTags -> add(mockk<SetTags>() to ExpectedHandler.TAG)
                            is PostsForTag -> add(mockk<PostsForTag>() to ExpectedHandler.TAG)

                            // Notes
                            RefreshNotes -> add(RefreshNotes to ExpectedHandler.NOTE)
                            is SetNotes -> add(mockk<SetNotes>() to ExpectedHandler.NOTE)
                            is SetNote -> add(mockk<SetNote>() to ExpectedHandler.NOTE)

                            // Popular
                            RefreshPopular -> add(RefreshPopular to ExpectedHandler.POPULAR)
                            is SetPopularPosts -> add(mockk<SetPopularPosts>() to ExpectedHandler.POPULAR)
                        }.let { } // to make it exhaustive
                    }
            }
    }

    @Nested
    inner class DuplicateContentTests {

        @Test
        fun `WHEN NavigationActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<NavigationAction>()
            coEvery {
                mockNavigationActionHandler.runAction(
                    mockAction,
                    expectedInitialValue
                )
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN PostActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<PostAction>()
            coEvery {
                mockPostActionHandler.runAction(
                    mockAction,
                    expectedInitialValue
                )
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN SearchActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<SearchAction>()
            coEvery {
                mockSearchActionHandler.runAction(
                    mockAction,
                    expectedInitialValue
                )
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN TagActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<TagAction>()
            coEvery {
                mockTagActionHandler.runAction(
                    mockAction,
                    expectedInitialValue
                )
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN NoteActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<NoteAction>()
            coEvery {
                mockNoteActionHandler.runAction(
                    mockAction,
                    expectedInitialValue
                )
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }

        @Test
        fun `WHEN PopularActionHandler return the same content THEN value is never updated`() {
            // GIVEN
            val mockAction = mockk<PopularAction>()
            coEvery {
                mockPopularActionHandler.runAction(
                    mockAction,
                    expectedInitialValue
                )
            } returns expectedInitialValue

            // WHEN
            runBlocking { appStateDataSource.runAction(mockAction) }

            // THEN
            coVerify(exactly = 0) { appStateDataSource.updateContent(any()) }
        }
    }

    @Test
    fun `WHEN getInitialContent is called THEN expected initial content is returned`() {
        assertThat(appStateDataSource.getInitialContent()).isEqualTo(expectedInitialValue)
    }

    @Test
    fun `GIVEN updateContent is called THEN getContent should return that value`() {
        // GIVEN
        val mockContent = mockk<Content>()

        // WHEN
        appStateDataSource.updateContent(mockContent)

        // THEN
        appStateDataSource.getContent().currentValueShouldBe(mockContent)
    }

    internal enum class ExpectedHandler {
        NAVIGATION, POST, SEARCH, TAG, NOTE, POPULAR
    }
}
