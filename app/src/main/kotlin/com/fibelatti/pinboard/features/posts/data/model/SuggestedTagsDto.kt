package com.fibelatti.pinboard.features.posts.data.model

data class SuggestedTagsDto(
    val popular: List<String>,
    val recommended: List<String>
)
