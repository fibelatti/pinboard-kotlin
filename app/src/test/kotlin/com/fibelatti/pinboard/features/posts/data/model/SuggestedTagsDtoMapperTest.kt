package com.fibelatti.pinboard.features.posts.data.model

import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createSuggestedTags
import com.fibelatti.pinboard.MockDataProvider.createSuggestedTagsDto
import org.junit.jupiter.api.Test

class SuggestedTagsDtoMapperTest {

    private val mapper = SuggestedTagDtoMapper()

    @Test
    fun `WHEN map is called THEN SuggestedTags is returned`() {
        mapper.map(createSuggestedTagsDto()) shouldBe createSuggestedTags()
    }
}
