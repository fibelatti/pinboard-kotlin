package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.collectIn
import com.fibelatti.pinboard.runUnconfinedTest
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class TagManagerViewModelTest : BaseViewModelTest() {

    private val viewModel = TagManagerViewModel()

    @Test
    fun `setTags emits the expected state`() = runUnconfinedTest {
        val states = viewModel.state.collectIn(this)
        val tags = listOf(mockk<Tag>())

        viewModel.initializeTags(tags)

        assertThat(states).containsExactly(
            TagManagerViewModel.State(),
            TagManagerViewModel.State(tags = tags),
        )
    }

    @Test
    fun `addTag emits the expected state - one tag`() = runUnconfinedTest {
        val states = viewModel.state.collectIn(this)

        viewModel.addTag("new-tag")

        assertThat(states).containsExactly(
            TagManagerViewModel.State(),
            TagManagerViewModel.State(tags = listOf(Tag(name = "new-tag"))),
        )
    }

    @Test
    fun `addTag emits the expected state - multiple tags`() = runUnconfinedTest {
        val states = viewModel.state.collectIn(this)

        viewModel.addTag("new-tag-1 new-tag-2")

        assertThat(states).containsExactly(
            TagManagerViewModel.State(),
            TagManagerViewModel.State(
                tags = listOf(Tag(name = "new-tag-1"), Tag(name = "new-tag-2")),
            ),
        )
    }

    @Test
    fun `addTag emits the expected state - existing tag`() = runUnconfinedTest {
        viewModel.initializeTags(listOf(Tag("new-tag")))

        val states = viewModel.state.collectIn(this)

        viewModel.addTag("new-tag")

        assertThat(states).containsExactly(
            TagManagerViewModel.State(tags = listOf(Tag(name = "new-tag"))),
        )
    }

    @Test
    fun `removeTag emits the expected state`() = runUnconfinedTest {
        val tag = mockk<Tag>()
        val tags = listOf(tag)

        viewModel.initializeTags(tags)

        val states = viewModel.state.collectIn(this)

        viewModel.removeTag(tag)

        assertThat(states).containsExactly(
            TagManagerViewModel.State(tags = tags),
            TagManagerViewModel.State(),
        )
    }

    @Test
    fun `setSuggestedTags emits the expectedState`() = runUnconfinedTest {
        val states = viewModel.state.collectIn(this)
        val tags = listOf("suggested")

        viewModel.setSuggestedTags(tags)

        assertThat(states).containsExactly(
            TagManagerViewModel.State(),
            TagManagerViewModel.State(suggestedTags = tags),
        )
    }

    @Test
    fun `setQuery emits the expectedState`() = runUnconfinedTest {
        val states = viewModel.state.collectIn(this)

        viewModel.setQuery("query")

        assertThat(states).containsExactly(
            TagManagerViewModel.State(),
            TagManagerViewModel.State(currentQuery = "query"),
        )
    }
}
