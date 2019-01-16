package com.fibelatti.pinboard.features.posts.domain.model

data class SuggestedTags(
    val popular: List<String>,
    val recommended: List<String>
)
