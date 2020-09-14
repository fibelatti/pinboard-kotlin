package com.fibelatti.pinboard.features.posts.domain

sealed class PostVisibility {
    object Public : PostVisibility()
    object Private : PostVisibility()
    object None : PostVisibility()
}
