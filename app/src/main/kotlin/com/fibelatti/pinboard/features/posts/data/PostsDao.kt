package com.fibelatti.pinboard.features.posts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fibelatti.core.extension.remove
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.data.model.POST_FTS_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PostDto

@Dao
interface PostsDao {

    @Query("delete from $POST_TABLE_NAME")
    fun deleteAllPosts()

    @Query("delete from $POST_TABLE_NAME where href = :url")
    fun deletePost(url: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePosts(posts: List<PostDto>)

    @Query("select count(*) from (select hash from $POST_TABLE_NAME $WHERE_SUB_QUERY limit :limit)")
    fun getPostCount(
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1
    ): Int

    @Suppress("LongParameterList")
    @Query("$SELECT_ALL_FROM_POST $WHERE_SUB_QUERY $ORDER_BY_SUB_QUERY limit :offset, :limit")
    fun getAllPosts(
        newestFirst: Boolean = true,
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
        offset: Int = 0
    ): List<PostDto>

    @Query("select tags from $POST_FTS_TABLE_NAME where tags match :tag")
    fun searchExistingPostTag(tag: String): List<String>

    @Query("select * from $POST_TABLE_NAME where href = :url")
    fun getPost(url: String): PostDto?

    @Query("select tags from $POST_TABLE_NAME where tags != ''")
    fun getAllPostTags(): List<String>

    companion object {

        private const val SELECT_ALL_FROM_POST = "select $POST_TABLE_NAME.* from $POST_TABLE_NAME"

        private const val MATCH_TERM = "case " +
            "when :term = '' then 1 " +
            "else $POST_TABLE_NAME.rowid in (" +
            "select rowid from $POST_FTS_TABLE_NAME where $POST_FTS_TABLE_NAME match :term" +
            ") " +
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

        private const val MATCH_UNTAGGED_OR_TAGS = "case " +
            "when :untaggedOnly = 1 then tags = '' " +
            "else $MATCH_TAGS " +
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

        private const val ORDER_BY_SUB_QUERY = "order by " +
            "case when :newestFirst = 1 then time end DESC, " +
            "case when :newestFirst = 0 then time end ASC"

        @JvmStatic
        fun preFormatTerm(term: String): String = term
            .replace("[^A-Za-z0-9 ._\\-=#@&]".toRegex(), "")
            .trim()
            .takeIf(String::isNotEmpty)
            ?.split(" ")
            ?.joinToString(separator = " NEAR ") { "$it*" }
            .orEmpty()

        @JvmStatic
        fun preFormatTag(tag: String) = "\"${tag.remove("\"")}*\""
    }
}
