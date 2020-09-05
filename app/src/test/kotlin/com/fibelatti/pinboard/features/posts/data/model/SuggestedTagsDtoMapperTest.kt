package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.pinboard.MockDataProvider.createSuggestedTags
import com.fibelatti.pinboard.MockDataProvider.createSuggestedTagsDto
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class SuggestedTagsDtoMapperTest {

    private val mapper = SuggestedTagDtoMapper()

    @Test
    fun `WHEN map is called THEN SuggestedTags is returned`() {
        assertThat(mapper.map(createSuggestedTagsDto())).isEqualTo(createSuggestedTags())
    }
}
