package com.fibelatti.pinboard.features.linkding.data

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions.TOKENIZER_UNICODE61
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocalFts.Companion.TABLE_NAME

@Fts4(
    contentEntity = BookmarkLocal::class,
    tokenizer = TOKENIZER_UNICODE61,
    tokenizerArgs = ["tokenchars=._-=#@&"],
)
@Entity(tableName = TABLE_NAME)
data class BookmarkLocalFts(
    val url: String,
    val title: String,
    val description: String,
    val notes: String?,
    val websiteTitle: String?,
    val websiteDescription: String?,
    val tagNames: String?,
) {

    companion object {

        const val TABLE_NAME = "LinkdingBookmarksFts"
    }
}
