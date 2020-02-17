package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions.TOKENIZER_UNICODE61

const val POST_FTS_TABLE_NAME = "PostsFts"

@Keep
@Fts4(
    contentEntity = PostDto::class,
    tokenizer = TOKENIZER_UNICODE61,
    tokenizerArgs = ["tokenchars=._-=#@&"]
)
@Entity(tableName = POST_FTS_TABLE_NAME)
data class PostDtoFts(
    val href: String,
    val description: String,
    val extended: String,
    val tags: String
)
