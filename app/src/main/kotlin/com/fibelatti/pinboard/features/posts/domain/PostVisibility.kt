package com.fibelatti.pinboard.features.posts.domain

sealed class PostVisibility {
    data object Public : PostVisibility()
    data object Private : PostVisibility()
    data object None : PostVisibility()
}
