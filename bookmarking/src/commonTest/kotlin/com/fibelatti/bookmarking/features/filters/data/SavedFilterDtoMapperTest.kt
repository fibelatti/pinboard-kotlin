package com.fibelatti.bookmarking.features.filters.data

import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SavedFilterDtoMapperTest {

    private val mapper = SavedFilterDtoMapper()

    @Test
    fun `map behaves as expected`() = runTest {
        val input = listOf(
            SavedFilterDto(term = "term", tags = "tag1,tag2"),
            SavedFilterDto(term = "term", tags = "tag1"),
            SavedFilterDto(term = "term", tags = ""),
        )
        val expectedOutput = listOf(
            SavedFilter(searchTerm = "term", tags = listOf(Tag("tag1"), Tag("tag2"))),
            SavedFilter(searchTerm = "term", tags = listOf(Tag("tag1"))),
            SavedFilter(searchTerm = "term", tags = listOf()),
        )

        assertThat(mapper.mapList(input)).isEqualTo(expectedOutput)
    }

    @Test
    fun `mapReverse behaves as expected`() = runTest {
        val input = listOf(
            SavedFilter(searchTerm = "term", tags = listOf(Tag("tag1"), Tag("tag2"))),
            SavedFilter(searchTerm = "term", tags = listOf(Tag("tag1"))),
            SavedFilter(searchTerm = "term", tags = listOf()),
        )
        val expectedOutput = listOf(
            SavedFilterDto(term = "term", tags = "tag1,tag2"),
            SavedFilterDto(term = "term", tags = "tag1"),
            SavedFilterDto(term = "term", tags = ""),
        )

        assertThat(mapper.mapListReverse(input)).isEqualTo(expectedOutput)
    }
}
