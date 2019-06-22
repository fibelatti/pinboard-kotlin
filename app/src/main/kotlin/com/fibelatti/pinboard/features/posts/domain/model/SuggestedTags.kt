package com.fibelatti.pinboard.features.posts.domain.model

import com.fibelatti.pinboard.features.tags.domain.model.Tag

data class SuggestedTags(
    val popular: List<Tag>,
    val recommended: List<Tag>
)
