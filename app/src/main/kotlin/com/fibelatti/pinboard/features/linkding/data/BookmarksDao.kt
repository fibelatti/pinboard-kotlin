package com.fibelatti.pinboard.features.linkding.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal.Companion.TABLE_NAME
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocalFts.Companion.TABLE_NAME as FTS_TABLE_NAME
import com.fibelatti.pinboard.features.posts.domain.PostVisibility

@Dao
interface BookmarksDao {

    @Query("delete from $TABLE_NAME")
    suspend fun deleteAllBookmarks()

    @Query("delete from $TABLE_NAME where pendingSync is null")
    suspend fun deleteAllSyncedBookmarks()

    @Query("delete from $TABLE_NAME where id = :id")
    suspend fun deleteBookmark(id: String)

    @Upsert
    suspend fun saveBookmarks(bookmarks: List<BookmarkLocal>)

    @RawQuery
    suspend fun getBookmarkCount(query: SupportSQLiteQuery = bookmarksCountFtsQuery()): Int

    @RawQuery
    suspend fun getAllBookmarks(query: SupportSQLiteQuery = allBookmarksFtsQuery()): List<BookmarkLocal>

    @RawQuery
    suspend fun searchExistingBookmarkTags(query: SupportSQLiteQuery): List<String>

    @Query("select * from $TABLE_NAME where id = :id or url = :url")
    suspend fun getBookmark(id: String, url: String): BookmarkLocal?

    @Query("select tagNames from $TABLE_NAME where tagNames != ''")
    suspend fun getAllBookmarkTags(): List<String>

    @Query("select * from $TABLE_NAME where pendingSync is not null")
    suspend fun getPendingSyncBookmarks(): List<BookmarkLocal>

    @Query("delete from $TABLE_NAME where url = :url and pendingSync is not null")
    suspend fun deletePendingSyncBookmark(url: String)

    companion object {

        // region FTS queries
        fun bookmarksCountFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return bookmarksFtsQuery(
                selectStatement = "select count(*) from (select id from $TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                limit = limit,
                addTrailingParenthesis = true,
            )
        }

        fun allBookmarksFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            sortType: Int = 0,
            offset: Int = 0,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return bookmarksFtsQuery(
                selectStatement = "select $TABLE_NAME.* from $TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                sortType = sortType,
                offset = offset,
                limit = limit,
            )
        }

        private fun bookmarksFtsQuery(
            selectStatement: String,
            term: String,
            tag1: String,
            tag2: String,
            tag3: String,
            untaggedOnly: Boolean,
            postVisibility: PostVisibility,
            readLaterOnly: Boolean,
            sortType: Int = 0,
            offset: Int = 0,
            limit: Int = -1,
            addTrailingParenthesis: Boolean = false,
        ): SimpleSQLiteQuery {
            val words: List<String> = term.trim()
                .split(regex = "\\s+".toRegex())
                .filterNot { it.isEmpty() }
            val sql: String = buildString {
                append(selectStatement)

                if (words.isNotEmpty()) {
                    val termStatement: String = "$TABLE_NAME.rowid in" +
                        " (select rowid from $FTS_TABLE_NAME where $FTS_TABLE_NAME match ?)" +
                        " or (" + words.joinToString(separator = " and ") { "$TABLE_NAME.url like ?" } + ")"

                    append(" and ($termStatement)")
                }

                if (untaggedOnly) {
                    append(" and tagNames = ''")
                } else {
                    val tagsStatement: String = " and $TABLE_NAME.rowid in" +
                        " (select rowid from $FTS_TABLE_NAME where tagNames match ?)"

                    if (tag1.isNotBlank()) append(tagsStatement)
                    if (tag2.isNotBlank()) append(tagsStatement)
                    if (tag3.isNotBlank()) append(tagsStatement)
                }

                when (postVisibility) {
                    is PostVisibility.Public -> append(" and shared = 1")
                    is PostVisibility.Private -> append(" and shared = 0")
                    is PostVisibility.None -> Unit
                }

                if (readLaterOnly) {
                    append(" and unread = 1")
                }

                when (sortType) {
                    0 -> append(" order by dateAdded DESC")
                    1 -> append(" order by dateAdded ASC")
                    2 -> append(" order by dateModified DESC")
                    3 -> append(" order by dateModified ASC")
                    4 -> append(" order by title ASC")
                    5 -> append(" order by title DESC")
                }

                append(" limit ?, ?")

                if (addTrailingParenthesis) {
                    append(")")
                }
            }
            val args: List<Any> = buildList {
                if (words.isNotEmpty()) {
                    add(words.joinToString(separator = " and ") { "*$it*" })
                    addAll(words.map { "%$it%" })
                }

                if (!untaggedOnly) {
                    if (tag1.isNotBlank()) add(formatTagArgument(tag1))
                    if (tag2.isNotBlank()) add(formatTagArgument(tag2))
                    if (tag3.isNotBlank()) add(formatTagArgument(tag3))
                }

                add(offset)
                add(limit)
            }

            return SimpleSQLiteQuery(query = sql, bindArgs = args.toTypedArray())
        }

        fun existingBookmarkTagFtsQuery(tag: String): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "select tagNames from $FTS_TABLE_NAME where tagNames match ?",
                bindArgs = arrayOf(formatTagArgument(tag = tag)),
            )
        }

        private fun formatTagArgument(tag: String): String {
            return "*${tag.replace(oldValue = "\"", newValue = "")}*"
        }
        // endregion FTS queries

        // region No FTS queries
        fun bookmarksCountNoFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return bookmarksNoFtsQuery(
                selectStatement = "select count(*) from (select id from $TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                limit = limit,
                addTrailingParenthesis = true,
            )
        }

        fun allBookmarksNoFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            sortType: Int = 0,
            offset: Int = 0,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return bookmarksNoFtsQuery(
                selectStatement = "select $TABLE_NAME.* from $TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                sortType = sortType,
                offset = offset,
                limit = limit,
            )
        }

        private fun bookmarksNoFtsQuery(
            selectStatement: String,
            term: String,
            tag1: String,
            tag2: String,
            tag3: String,
            untaggedOnly: Boolean,
            postVisibility: PostVisibility,
            readLaterOnly: Boolean,
            sortType: Int = -1,
            offset: Int = 0,
            limit: Int = -1,
            addTrailingParenthesis: Boolean = false,
        ): SimpleSQLiteQuery {
            val termColumns: List<String> = listOf(
                "$TABLE_NAME.url",
                "$TABLE_NAME.title",
                "$TABLE_NAME.description",
                "$TABLE_NAME.notes",
                "$TABLE_NAME.websiteTitle",
                "$TABLE_NAME.websiteDescription",
                "$TABLE_NAME.tagNames",
            )
            val words: List<String> = term.trim()
                .split(regex = "\\s+".toRegex())
                .filterNot { it.isEmpty() }
            val sql: String = buildString {
                append(selectStatement)

                if (words.isNotEmpty()) {
                    val termStatement: String = termColumns.joinToString(separator = " or ") { columnName ->
                        "(" + words.joinToString(separator = " and ") { "$columnName like ?" } + ")"
                    }

                    append(" and ($termStatement)")
                }

                if (untaggedOnly) {
                    append(" and tagNames = ''")
                } else {
                    if (tag1.isNotBlank()) append(" and $TABLE_NAME.tagNames like ?")
                    if (tag2.isNotBlank()) append(" and $TABLE_NAME.tagNames like ?")
                    if (tag3.isNotBlank()) append(" and $TABLE_NAME.tagNames like ?")
                }

                when (postVisibility) {
                    is PostVisibility.Public -> append(" and shared = 1")
                    is PostVisibility.Private -> append(" and shared = 0")
                    is PostVisibility.None -> Unit
                }

                if (readLaterOnly) {
                    append(" and unread = 1")
                }

                when (sortType) {
                    0 -> append(" order by dateAdded DESC")
                    1 -> append(" order by dateAdded ASC")
                    2 -> append(" order by dateModified DESC")
                    3 -> append(" order by dateModified ASC")
                    4 -> append(" order by title ASC")
                    5 -> append(" order by title DESC")
                }

                append(" limit ?, ?")

                if (addTrailingParenthesis) {
                    append(")")
                }
            }
            val args: List<Any> = buildList {
                if (words.isNotEmpty()) {
                    repeat(times = termColumns.size) {
                        addAll(words.map { "%$it%" })
                    }
                }

                if (!untaggedOnly) {
                    if (tag1.isNotBlank()) add("%$tag1%")
                    if (tag2.isNotBlank()) add("%$tag2%")
                    if (tag3.isNotBlank()) add("%$tag3%")
                }

                add(offset)
                add(limit)
            }

            return SimpleSQLiteQuery(query = sql, bindArgs = args.toTypedArray())
        }

        fun existingBookmarkTagNoFtsQuery(tag: String): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "select tagNames from $TABLE_NAME where tagNames like ?",
                bindArgs = arrayOf("%$tag%"),
            )
        }
        // endregion No FTS queries
    }
}
