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
                    val termStatement: String = "$POST_TABLE_NAME.rowid in" +
                        " (select rowid from $POST_FTS_TABLE_NAME where $POST_FTS_TABLE_NAME match ?)" +
                        " or (" + words.joinToString(separator = " and ") { "$POST_TABLE_NAME.href like ?" } + ")"

                    append(" and ($termStatement)")
                }

                if (untaggedOnly) {
                    append(" and tags = ''")
                } else {
                    val tagsStatement: String = " and $POST_TABLE_NAME.rowid in" +
                        " (select rowid from $POST_FTS_TABLE_NAME where tags match ?)"

                    if (tag1.isNotBlank()) append(tagsStatement)
                    if (tag2.isNotBlank()) append(tagsStatement)
                    if (tag3.isNotBlank()) append(tagsStatement)
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

        fun existingPostTagFtsQuery(tag: String): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "select tags from $POST_FTS_TABLE_NAME where tags match ?",
                bindArgs = arrayOf(formatTagArgument(tag = tag)),
            )
        }

        private fun formatTagArgument(tag: String): String {
            return "*${tag.replace(oldValue = "\"", newValue = "")}*"
        }
        // endregion FTS queries

        // region No FTS queries
        fun postCountNoFtsQuery(
            term: String = "",
            tag1: String = "",
            tag2: String = "",
            tag3: String = "",
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
            val sql: String = buildString {
                append(selectStatement)

                if (words.isNotEmpty()) {
                    val termStatement: String = termColumns.joinToString(separator = " or ") { columnName ->
                        "(" + words.joinToString(separator = " and ") { "$columnName like ?" } + ")"
                    }

                    append(" and ($termStatement)")
                }

                if (untaggedOnly) {
                    append(" and tags = ''")
                } else {
                    if (tag1.isNotBlank()) append(" and $POST_TABLE_NAME.tags like ?")
                    if (tag2.isNotBlank()) append(" and $POST_TABLE_NAME.tags like ?")
                    if (tag3.isNotBlank()) append(" and $POST_TABLE_NAME.tags like ?")
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

        fun existingPostTagNoFtsQuery(tag: String): SimpleSQLiteQuery {
            return SimpleSQLiteQuery(
                query = "select tags from $POST_TABLE_NAME where tags like ?",
                bindArgs = arrayOf("%$tag%"),
            )
        }
        // endregion No FTS queries
    }
}
