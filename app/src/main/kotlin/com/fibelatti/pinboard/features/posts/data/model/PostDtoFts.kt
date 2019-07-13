package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4

const val POST_FTS_TABLE_NAME = "PostsFts"

@Keep
@Fts4(contentEntity = PostDto::class)
@Entity(tableName = POST_FTS_TABLE_NAME)
data class PostDtoFts(
    val href: String,
    val description: String,
    val extended: String,
    val tags: String
)
