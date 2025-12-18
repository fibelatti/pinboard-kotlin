package com.fibelatti.pinboard.features.filters.data

import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SavedFilterDtoMapperTest {

    private val mapper = SavedFilterDtoMapper()

    @Test
    fun `map behaves as expected`() = runTest {
        val matchAll = randomBoolean()
        val exactMatch = randomBoolean()

        val input = listOf(
            SavedFilterDto(term = "term", tags = "tag1,tag2", matchAll = matchAll, exactMatch = exactMatch),
            SavedFilterDto(term = "term", tags = "tag1", matchAll = matchAll, exactMatch = exactMatch),
            SavedFilterDto(term = "term", tags = "", matchAll = matchAll, exactMatch = exactMatch),
        )
        val expectedOutput = listOf(
            SavedFilter(
                term = "term",
                tags = listOf(Tag("tag1"), Tag("tag2")),
                matchAll = matchAll,
                exactMatch = exactMatch,
            ),
            SavedFilter(term = "term", tags = listOf(Tag("tag1")), matchAll = matchAll, exactMatch = exactMatch),
            SavedFilter(term = "term", tags = listOf(), matchAll = matchAll, exactMatch = exactMatch),
        )

        assertThat(mapper.mapList(input)).isEqualTo(expectedOutput)
    }

    @Test
    fun `mapReverse behaves as expected`() = runTest {
        val matchAll = randomBoolean()
        val exactMatch = randomBoolean()

        val input = listOf(
            SavedFilter(
                term = "term",
                tags = listOf(Tag("tag1"), Tag("tag2")),
                matchAll = matchAll,
                exactMatch = exactMatch,
            ),
            SavedFilter(term = "term", tags = listOf(Tag("tag1")), matchAll = matchAll, exactMatch = exactMatch),
            SavedFilter(term = "term", tags = listOf(), matchAll = matchAll, exactMatch = exactMatch),
        )
        val expectedOutput = listOf(
            SavedFilterDto(term = "term", tags = "tag1,tag2", matchAll = matchAll, exactMatch = exactMatch),
            SavedFilterDto(term = "term", tags = "tag1", matchAll = matchAll, exactMatch = exactMatch),
            SavedFilterDto(term = "term", tags = "", matchAll = matchAll, exactMatch = exactMatch),
        )

        assertThat(mapper.mapListReverse(input)).isEqualTo(expectedOutput)
    }
}
