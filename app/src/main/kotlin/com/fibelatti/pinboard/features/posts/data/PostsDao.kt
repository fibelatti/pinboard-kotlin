package com.fibelatti.pinboard.features.posts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PostDto

private const val WHERE_TERM = "(" +
    "href like '%' || :term || '%' " +
    "or description like '%' || :term || '%' " +
    "or extended like '%' || :term || '%'" +
    ")"

private const val WHERE_TAGS = "(" +
    "tags like '%' || :tag1 || '%' " +
    "and tags like '%' || :tag2 || '%' " +
    "and tags like '%' || :tag3 || '%'" +
    ")"

private const val WHERE_UNTAGGED_OR_TAGS = "case when :untaggedOnly = 1 then tags = '' else $WHERE_TAGS end"

private const val WHERE_PUBLIC = "case " +
    "when :publicPostsOnly = 1 then shared = '${AppConfig.PinboardApiLiterals.YES}' " +
    "when :privatePostsOnly = 1 then shared = '${AppConfig.PinboardApiLiterals.NO}' " +
    "else 1 " +
    "end"

private const val WHERE_READ_LATER = "case " +
    "when :readLaterOnly = 1 then toread = '${AppConfig.PinboardApiLiterals.YES}' " +
    "else 1 " +
    "end"

private const val WHERE_SUB_QUERY = "where $WHERE_TERM and $WHERE_UNTAGGED_OR_TAGS and $WHERE_PUBLIC and $WHERE_READ_LATER"

private const val ORDER_BY_SUB_QUERY = "order by " +
    "case when :newestFirst = 1 then time end DESC, " +
    "case when :newestFirst = 0 then time end ASC"

private const val LIMIT_SUB_QUERY = "limit :limit"

@Dao
interface PostsDao {

    @Query("delete from $POST_TABLE_NAME")
    fun deleteAllPosts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePosts(posts: List<PostDto>)

    @Query("select count(*) from (select hash from $POST_TABLE_NAME $WHERE_SUB_QUERY $LIMIT_SUB_QUERY)")
    fun getPostCount(
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1
    ): Int

    @Query("select * from $POST_TABLE_NAME $WHERE_SUB_QUERY $ORDER_BY_SUB_QUERY $LIMIT_SUB_QUERY")
    fun getAllPosts(
        newestFirst: Boolean = true,
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        publicPostsOnly: Boolean = false,
        privatePostsOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1
    ): List<PostDto>
}
