package com.fibelatti.pinboard.features.tags.data

import app.cash.turbine.test
import com.fibelatti.core.functional.Success
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
import com.fibelatti.pinboard.MockDataProvider.createAppState
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.receivedItems
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class TagManagerDataSourceTest : BaseViewModelTest() {

    private val appStateFlow = MutableStateFlow(createAppState())
    private val mockAppStateRepository = mockk<AppStateRepository> {
        every { appState } returns appStateFlow
    }
    private val mockPostsRepository = mockk<PostsRepository>()

    private val dataSource = TagManagerDataSource(
        scope = TestScope(dispatcher),
        appStateRepository = mockAppStateRepository,
        postsRepository = mockPostsRepository,
    )

    @Test
    fun `init sets the initial state value for AddPostContent`() = runTest {
        dataSource.tagManagerState.test {
            val content = {
                AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = SAMPLE_TAGS,
                    previousContent = mockk(),
                )
            }

            appStateFlow.value = createAppState(content = content())
            appStateFlow.value = createAppState(content = content())

            assertThat(expectMostRecentItem()).isEqualTo(TagManagerState(tags = SAMPLE_TAGS))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `init sets the initial state value for EditPostContent`() = runTest {
        dataSource.tagManagerState.test {
            val content = {
                EditPostContent(
                    post = mockk {
                        every { tags } returns SAMPLE_TAGS
                    },
                    previousContent = mockk(),
                )
            }

            appStateFlow.value = createAppState(content = content())
            appStateFlow.value = createAppState(content = content())

            assertThat(expectMostRecentItem()).isEqualTo(TagManagerState(tags = SAMPLE_TAGS))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `init sets the initial state value for UserPreferencesContent`() = runTest {
        dataSource.tagManagerState.test {
            val content = {
                mockk<UserPreferencesContent> {
                    every { userPreferences } returns mockk {
                        every { defaultTags } returns SAMPLE_TAGS
                    }
                }
            }

            appStateFlow.value = createAppState(content = content())
            appStateFlow.value = createAppState(content = content())

            assertThat(expectMostRecentItem()).isEqualTo(TagManagerState(tags = SAMPLE_TAGS))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `state emissions trigger a search for suggested tags`() = runTest {
        dataSource.tagManagerState.test {
            coEvery {
                mockPostsRepository.searchExistingPostTag(tag = "", currentTags = emptyList())
            } returns Success(listOf("tag-1", "tag-2"))
            coEvery {
                mockPostsRepository.searchExistingPostTag(tag = "", currentTags = listOf(Tag("tag-1")))
            } returns Success(listOf("tag-2"))

            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = emptyList(),
                    previousContent = mockk(),
                ),
            )

            dataSource.addTag("tag-1")

            assertThat(receivedItems()).containsExactly(
                TagManagerState(suggestedTags = listOf("tag-1", "tag-2")),
                TagManagerState(tags = listOf(Tag("tag-1")), suggestedTags = listOf("tag-2")),
            )
        }
    }

    @Test
    fun `addTag emits the expected state - one tag`() = runTest {
        dataSource.tagManagerState.test {
            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = emptyList(),
                    previousContent = mockk(),
                ),
            )

            dataSource.addTag("new-tag")

            assertThat(expectMostRecentItem()).isEqualTo(
                TagManagerState(tags = listOf(Tag(name = "new-tag"))),
            )
        }
    }

    @Test
    fun `addTag emits the expected state - multiple tags`() = runTest {
        dataSource.tagManagerState.test {
            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = emptyList(),
                    previousContent = mockk(),
                ),
            )

            dataSource.addTag("new-tag-1 new-tag-2")

            assertThat(expectMostRecentItem()).isEqualTo(
                TagManagerState(tags = listOf(Tag(name = "new-tag-1"), Tag(name = "new-tag-2"))),
            )
        }
    }

    @Test
    fun `addTag emits the expected state - existing tag`() = runTest {
        dataSource.tagManagerState.test {
            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = listOf(Tag("new-tag")),
                    previousContent = mockk(),
                ),
            )

            dataSource.addTag("new-tag")

            assertThat(expectMostRecentItem()).isEqualTo(
                TagManagerState(tags = listOf(Tag(name = "new-tag"))),
            )
        }
    }

    @Test
    fun `removeTag emits the expected state`() = runTest {
        dataSource.tagManagerState.test {
            val tag = mockk<Tag>()
            val tags = listOf(tag)

            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = tags,
                    previousContent = mockk(),
                ),
            )

            dataSource.removeTag(tag)

            assertThat(receivedItems()).containsExactly(
                TagManagerState(tags = tags),
                TagManagerState(),
            )
        }
    }

    @Test
    fun `setQuery emits the expectedState`() = runTest {
        dataSource.tagManagerState.test {
            appStateFlow.value = createAppState(
                content = AddPostContent(
                    defaultPrivate = randomBoolean(),
                    defaultReadLater = randomBoolean(),
                    defaultTags = emptyList(),
                    previousContent = mockk(),
                ),
            )

            dataSource.setTagSearchQuery("query")

            assertThat(expectMostRecentItem()).isEqualTo(
                TagManagerState(currentQuery = "query"),
            )
        }
    }
}
