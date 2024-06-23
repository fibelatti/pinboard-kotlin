package com.fibelatti.bookmarking.features.posts.domain

public sealed class PostVisibility {
    public data object Public : PostVisibility()
    public data object Private : PostVisibility()
    public data object None : PostVisibility()
}
