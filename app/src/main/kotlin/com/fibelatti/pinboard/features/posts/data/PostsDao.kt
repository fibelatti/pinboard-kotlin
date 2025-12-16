package com.fibelatti.pinboard.features.posts.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.data.model.POST_FTS_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.domain.PostVisibility

@Dao
interface PostsDao {

    @Query("delete from $POST_TABLE_NAME")
    suspend fun deleteAllPosts()

    @Query("delete from $POST_TABLE_NAME where pendingSync is null")
    suspend fun deleteAllSyncedPosts()

    @Query("delete from $POST_TABLE_NAME where href = :url")
    suspend fun deletePost(url: String)

    @Upsert
    suspend fun savePosts(posts: List<PostDto>)

    @RawQuery
    suspend fun getPostCount(query: SupportSQLiteQuery = postCountFtsQuery()): Int

    @RawQuery
    suspend fun getAllPosts(query: SupportSQLiteQuery = allPostsFtsQuery()): List<PostDto>

    @RawQuery
    suspend fun searchExistingPostTag(query: SupportSQLiteQuery): List<String>

    @Query("select * from $POST_TABLE_NAME where href = :url")
    suspend fun getPost(url: String): PostDto?

    @Query("select tags from $POST_TABLE_NAME where tags != ''")
    suspend fun getAllPostTags(): List<String>

    @Query("select * from $POST_TABLE_NAME where pendingSync is not null")
    suspend fun getPendingSyncPosts(): List<PostDto>

    @Query("delete from $POST_TABLE_NAME where href = :url and pendingSync is not null")
    suspend fun deletePendingSyncPost(url: String)

    companion object {

        // region FTS queries
        fun postCountFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            matchAll: Boolean = true,
            exactMatch: Boolean = false,
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return postFtsQuery(
                selectStatement = "select count(*) from (select hash from $POST_TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                limit = limit,
                addTrailingParenthesis = true,
            )
        }

        fun allPostsFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            matchAll: Boolean = true,
            exactMatch: Boolean = false,
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            sortType: Int = 0,
            offset: Int = 0,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return postFtsQuery(
                selectStatement = "select $POST_TABLE_NAME.* from $POST_TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                sortType = sortType,
                offset = offset,
                limit = limit,
            )
        }

        private fun postFtsQuery(
            selectStatement: String,
            term: String,
            tag1: String,
            tag2: String,
            tag3: String,
            matchAll: Boolean,
            exactMatch: Boolean,
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
            val logicalOperator: String = if (matchAll) "and" else "or"

            val filterSubquery: String = buildString {
                if (words.isNotEmpty()) {
                    val termSubstatement: String = words.joinToString(separator = " $logicalOperator ") {
                        "$POST_TABLE_NAME.href like ?"
                    }
                    val termStatement: String = "$POST_TABLE_NAME.rowid in" +
                        " (select rowid from $POST_FTS_TABLE_NAME where $POST_FTS_TABLE_NAME match ?)" +
                        " or ($termSubstatement)"

                    append("($termStatement)")
                }

                val tagsStatement: String = "$POST_TABLE_NAME.rowid in" +
                    " (select rowid from $POST_FTS_TABLE_NAME where tags match ?)"
                val tags: String = listOf(tag1, tag2, tag3)
                    .filter { it.isNotBlank() && !untaggedOnly }
                    .joinToString(separator = " $logicalOperator ") { tagsStatement }

                if (isNotEmpty() && tags.isNotEmpty()) {
                    append(" $logicalOperator ")
                }

                append(tags)
            }

            val sql: String = buildString {
                append(selectStatement)

                if (filterSubquery.isNotEmpty()) {
                    append(" and ($filterSubquery)")
                }

                if (untaggedOnly) {
                    append(" and tags = ''")
                }

                when (postVisibility) {
                    is PostVisibility.Public -> append(" and shared = '${AppConfig.PinboardApiLiterals.YES}'")
                    is PostVisibility.Private -> append(" and shared = '${AppConfig.PinboardApiLiterals.NO}'")
                    is PostVisibility.None -> Unit
                }

                if (readLaterOnly) {
                    append(" and toread = '${AppConfig.PinboardApiLiterals.YES}'")
                }

                when (sortType) {
                    0 -> append(" order by time DESC")
                    1 -> append(" order by time ASC")
                    4 -> append(" order by description ASC")
                    5 -> append(" order by description DESC")
                }

                append(" limit ?, ?")

                if (addTrailingParenthesis) {
                    append(")")
                }
            }
            val args: List<Any> = buildList {
                if (words.isNotEmpty()) {
                    add(
                        words.joinToString(separator = " $logicalOperator ") { word: String ->
                            if (exactMatch) word else "*$word*"
                        },
                    )
                    addAll(
                        words.map { word: String ->
                            if (exactMatch) word else "%$word%"
                        },
                    )
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

        fun existingPostTagFtsQuery(tag: String): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "select tags from $POST_FTS_TABLE_NAME where tags match ?",
                bindArgs = arrayOf(formatTagArgument(tag = tag, exactMatch = false)),
            )
        }

        private fun formatTagArgument(tag: String, exactMatch: Boolean = true): String {
            val sanitizedTag: String = tag.replace(oldValue = "\"", newValue = "")
            return if (exactMatch) sanitizedTag else "*$sanitizedTag*"
        }
        // endregion FTS queries

        // region No FTS queries
        fun postCountNoFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            matchAll: Boolean = true,
            exactMatch: Boolean = false,
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return postNoFtsQuery(
                selectStatement = "select count(*) from (select hash from $POST_TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                limit = limit,
                addTrailingParenthesis = true,
            )
        }

        fun allPostsNoFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
            matchAll: Boolean = true,
            exactMatch: Boolean = false,
            untaggedOnly: Boolean = false,
            postVisibility: PostVisibility = PostVisibility.None,
            readLaterOnly: Boolean = false,
            sortType: Int = 0,
            offset: Int = 0,
            limit: Int = -1,
        ): SimpleSQLiteQuery {
            return postNoFtsQuery(
                selectStatement = "select $POST_TABLE_NAME.* from $POST_TABLE_NAME where 1=1",
                term = term,
                tag1 = tag1,
                tag2 = tag2,
                tag3 = tag3,
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                sortType = sortType,
                offset = offset,
                limit = limit,
            )
        }

        private fun postNoFtsQuery(
            selectStatement: String,
            term: String,
            tag1: String,
            tag2: String,
            tag3: String,
            matchAll: Boolean,
            exactMatch: Boolean,
            untaggedOnly: Boolean,
            postVisibility: PostVisibility,
            readLaterOnly: Boolean,
            sortType: Int = -1,
            offset: Int = 0,
            limit: Int = -1,
            addTrailingParenthesis: Boolean = false,
        ): SimpleSQLiteQuery {
            val termColumns: List<String> = listOf(
                "$POST_TABLE_NAME.href",
                "$POST_TABLE_NAME.description",
                "$POST_TABLE_NAME.extended",
                "$POST_TABLE_NAME.tags",
            )
            val words: List<String> = term.trim()
                .split(regex = "\\s+".toRegex())
                .filterNot { it.isEmpty() }
            val logicalOperator: String = if (matchAll) "and" else "or"

            val filterSubquery: String = buildString {
                if (words.isNotEmpty()) {
                    val termStatement: String = termColumns.joinToString(separator = " or ") { columnName: String ->
                        "(" + words.joinToString(separator = " $logicalOperator ") { "$columnName like ?" } + ")"
                    }

                    append("($termStatement)")
                }

                val tags: String = listOf(tag1, tag2, tag3)
                    .filter { it.isNotBlank() && !untaggedOnly }
                    .joinToString(separator = " $logicalOperator ") { "$POST_TABLE_NAME.tags like ?" }

                if (isNotEmpty() && tags.isNotEmpty()) {
                    append(" $logicalOperator ")
                }

                append(tags)
            }

            val sql: String = buildString {
                append(selectStatement)

                if (filterSubquery.isNotEmpty()) {
                    append(" and ($filterSubquery)")
                }

                if (untaggedOnly) {
                    append(" and tags = ''")
                }

                when (postVisibility) {
                    is PostVisibility.Public -> append(" and shared = '${AppConfig.PinboardApiLiterals.YES}'")
                    is PostVisibility.Private -> append(" and shared = '${AppConfig.PinboardApiLiterals.NO}'")
                    is PostVisibility.None -> Unit
                }

                if (readLaterOnly) {
                    append(" and toread = '${AppConfig.PinboardApiLiterals.YES}'")
                }

                when (sortType) {
                    0 -> append(" order by time DESC")
                    1 -> append(" order by time ASC")
                    4 -> append(" order by description ASC")
                    5 -> append(" order by description DESC")
                }

                append(" limit ?, ?")

                if (addTrailingParenthesis) {
                    append(")")
                }
            }
            val args: List<Any> = buildList {
                if (words.isNotEmpty()) {
                    repeat(times = termColumns.size) {
                        addAll(
                            words.map { word: String ->
                                if (exactMatch) word else "%$word%"
                            },
                        )
                    }
                }

                if (!untaggedOnly) {
                    if (tag1.isNotBlank()) add(tag1)
                    if (tag2.isNotBlank()) add(tag2)
                    if (tag3.isNotBlank()) add(tag3)
                }

                add(offset)
                add(limit)
            }

            return SimpleSQLiteQuery(query = sql, bindArgs = args.toTypedArray())
        }

        fun existingPostTagNoFtsQuery(tag: String): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "select tags from $POST_TABLE_NAME where tags like ?",
                bindArgs = arrayOf("%$tag%"),
            )
        }
        // endregion No FTS queries
    }
}
