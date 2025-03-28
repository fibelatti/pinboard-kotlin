package com.fibelatti.pinboard.features.posts.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.persistence.database.isFtsCompatible
import com.fibelatti.pinboard.features.posts.data.model.POST_FTS_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PostDto

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

    @Query("select count(*) from (select hash from $POST_TABLE_NAME $WHERE_SUB_QUERY limit :limit)")
    suspend fun getPostCount(
        term: String = "",
        termNoFts: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
    ): Int

    @Query("select count(*) from (select hash from $POST_TABLE_NAME $WHERE_SUB_QUERY_NO_FTS limit :limit)")
    suspend fun getPostCountNoFts(
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
    ): Int

    @Query("$SELECT_ALL_FROM_POST $WHERE_SUB_QUERY $ORDER_BY_SUB_QUERY limit :offset, :limit")
    suspend fun getAllPosts(
        sortType: Int = 0,
        term: String = "",
        termNoFts: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
        offset: Int = 0,
    ): List<PostDto>

    @Query("$SELECT_ALL_FROM_POST $WHERE_SUB_QUERY_NO_FTS $ORDER_BY_SUB_QUERY limit :offset, :limit")
    suspend fun getAllPostsNoFts(
        sortType: Int = 0,
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
        offset: Int = 0,
    ): List<PostDto>

    @Query("select tags from $POST_FTS_TABLE_NAME where tags match :tag")
    suspend fun searchExistingPostTag(tag: String): List<String>

    @Query("select * from $POST_TABLE_NAME where href = :url")
    suspend fun getPost(url: String): PostDto?

    @Query("select tags from $POST_TABLE_NAME where tags != ''")
    suspend fun getAllPostTags(): List<String>

    @Query("select * from $POST_TABLE_NAME where pendingSync is not null")
    suspend fun getPendingSyncPosts(): List<PostDto>

    @Query("delete from $POST_TABLE_NAME where href = :url and pendingSync is not null")
    suspend fun deletePendingSyncPost(url: String)

    companion object {

        private const val SELECT_ALL_FROM_POST = "select $POST_TABLE_NAME.* from $POST_TABLE_NAME"

        private const val MATCH_TERM = "case " +
            "when :term = '' then 1 " +
            "else ($POST_TABLE_NAME.rowid in (" +
            "select rowid from $POST_FTS_TABLE_NAME where $POST_FTS_TABLE_NAME match :term" +
            ") OR " +
            "$POST_TABLE_NAME.href like '%' || :termNoFts || '%') " +
            "end"

        private const val LIKE_TERM = "case " +
            "when :term = '' then 1 " +
            "else ($POST_TABLE_NAME.href like '%' || :term || '%' or " +
            "$POST_TABLE_NAME.description like '%' || :term || '%' or " +
            "$POST_TABLE_NAME.extended like '%' || :term || '%') " +
            "end"

        private const val MATCH_TAGS = "case " +
            "when :tag1 = '' then 1 " +
            "else $POST_TABLE_NAME.rowid in (select rowid from $POST_FTS_TABLE_NAME where tags match :tag1) " +
            "end " +
            "and case " +
            "when :tag2 = '' then 1 " +
            "else $POST_TABLE_NAME.rowid in (select rowid from $POST_FTS_TABLE_NAME where tags match :tag2) " +
            "end " +
            "and case " +
            "when :tag3 = '' then 1 " +
            "else $POST_TABLE_NAME.rowid in (select rowid from $POST_FTS_TABLE_NAME where tags match :tag3) " +
            "end "

        private const val LIKE_TAGS = "case " +
            "when :tag1 = '' then 1 " +
            "else $POST_TABLE_NAME.tags like '%' || :tag1 || '%' " +
            "end " +
            "and case " +
            "when :tag2 = '' then 1 " +
            "else $POST_TABLE_NAME.tags like '%' || :tag2 || '%' " +
            "end " +
            "and case " +
            "when :tag3 = '' then 1 " +
            "else $POST_TABLE_NAME.tags like '%' || :tag3 || '%' " +
            "end "

        private const val MATCH_UNTAGGED_OR_TAGS = "case " +
            "when :untaggedOnly = 1 then tags = '' " +
            "else $MATCH_TAGS " +
            "end"

        private const val LIKE_UNTAGGED_OR_TAGS = "case " +
            "when :untaggedOnly = 1 then tags = '' " +
            "else $LIKE_TAGS " +
            "end"

        private const val WHERE_PUBLIC = "case " +
            "when :publicPostsOnly = 1 then shared = '${AppConfig.PinboardApiLiterals.YES}' " +
            "when :privatePostsOnly = 1 then shared = '${AppConfig.PinboardApiLiterals.NO}' " +
            "when :ignoreVisibility = 1 then 1 " +
            "end"

        private const val WHERE_READ_LATER = "case " +
            "when :readLaterOnly = 1 then toread = '${AppConfig.PinboardApiLiterals.YES}' " +
            "else 1 " +
            "end"

        private const val WHERE_SUB_QUERY = "where $MATCH_TERM " +
            "and $MATCH_UNTAGGED_OR_TAGS " +
            "and $WHERE_PUBLIC " +
            "and $WHERE_READ_LATER"

        private const val WHERE_SUB_QUERY_NO_FTS = "where $LIKE_TERM " +
            "and $LIKE_UNTAGGED_OR_TAGS " +
            "and $WHERE_PUBLIC " +
            "and $WHERE_READ_LATER"

        private const val ORDER_BY_SUB_QUERY = "order by " +
            "case when :sortType = 0 then time end DESC, " +
            "case when :sortType = 1 then time end ASC," +
            "case when :sortType = 4 then description end ASC," +
            "case when :sortType = 5 then description end DESC"

        @JvmStatic
        fun preFormatTerm(term: String): String {
            require(isFtsCompatible(term))

            return term.trim().takeIf(String::isNotEmpty)
                ?.split(" ")?.joinToString(separator = " NEAR ") { "$it*" }
                .orEmpty()
        }

        @JvmStatic
        fun preFormatTag(tag: String): String {
            require(isFtsCompatible(tag))

            return "\"${tag.replace(oldValue = "\"", newValue = "")}*\""
        }
    }
}
