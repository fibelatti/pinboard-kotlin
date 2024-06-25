package com.fibelatti.bookmarking.pinboard.data

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions.TOKENIZER_UNICODE61
import kotlinx.serialization.Serializable

public const val POST_FTS_TABLE_NAME: String = "PostsFts"

@Serializable
@Fts4(
    contentEntity = PostDto::class,
    tokenizer = TOKENIZER_UNICODE61,
    tokenizerArgs = ["tokenchars=._-=#@&"],
)
@Entity(tableName = POST_FTS_TABLE_NAME)
internal data class PostDtoFts(
    val href: String,
    val description: String?,
    val extended: String?,
    val tags: String,
)
