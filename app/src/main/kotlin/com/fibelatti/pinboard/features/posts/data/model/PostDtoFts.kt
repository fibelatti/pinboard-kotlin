package com.fibelatti.pinboard.features.posts.data.model

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Ignore

const val POST_FTS_TABLE_NAME = "PostsFts"

@Fts4(contentEntity = PostDto::class)
@Entity(tableName = POST_FTS_TABLE_NAME)
data class PostDtoFts(
    val href: String,
    val description: String,
    val extended: String,
    val tags: String
) {

    @Ignore
    constructor() : this(
        href = "",
        description = "",
        extended = "",
        tags = ""
    )
}
