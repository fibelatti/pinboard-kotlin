package com.fibelatti.pinboard.features.linkding.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fibelatti.pinboard.core.persistence.database.isFtsCompatible
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal.Companion.TABLE_NAME
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocalFts.Companion.TABLE_NAME as FTS_TABLE_NAME

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

    @Query("select count(*) from (select id from $TABLE_NAME $WHERE_SUB_QUERY limit :limit)")
    suspend fun getBookmarkCount(
        term: String = "",
        termNoFts: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicBookmarksOnly: Boolean = false,
        privateBookmarksOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
    ): Int

    @Query("select count(*) from (select id from $TABLE_NAME $WHERE_SUB_QUERY_NO_FTS limit :limit)")
    suspend fun getBookmarkCountNoFts(
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicBookmarksOnly: Boolean = false,
        privateBookmarksOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
    ): Int

    @Query("$SELECT_ALL_FROM_BOOKMARKS $WHERE_SUB_QUERY $ORDER_BY_SUB_QUERY limit :offset, :limit")
    suspend fun getAllBookmarks(
        sortType: Int = 0,
        term: String = "",
        termNoFts: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicBookmarksOnly: Boolean = false,
        privateBookmarksOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
        offset: Int = 0,
    ): List<BookmarkLocal>

    @Query("$SELECT_ALL_FROM_BOOKMARKS $WHERE_SUB_QUERY_NO_FTS $ORDER_BY_SUB_QUERY limit :offset, :limit")
    suspend fun getAllBookmarksNoFts(
        sortType: Int = 0,
        term: String = "",
        tag1: String = "",
        tag2: String = "",
        tag3: String = "",
        untaggedOnly: Boolean = false,
        ignoreVisibility: Boolean = true,
        publicBookmarksOnly: Boolean = false,
        privateBookmarksOnly: Boolean = false,
        readLaterOnly: Boolean = false,
        limit: Int = -1,
        offset: Int = 0,
    ): List<BookmarkLocal>

    @Query("select tagNames from $FTS_TABLE_NAME where tagNames match :tag")
    suspend fun searchExistingBookmarkTags(tag: String): List<String>

    @Query("select tagNames from $TABLE_NAME where tagNames like '%' || :tag || '%'")
    suspend fun searchExistingBookmarkTagsNoFts(tag: String): List<String>

    @Query("select * from $TABLE_NAME where id = :id or url = :url")
    suspend fun getBookmark(id: String, url: String): BookmarkLocal?

    @Query("select tagNames from $TABLE_NAME where tagNames != ''")
    suspend fun getAllBookmarkTags(): List<String>

    @Query("select * from $TABLE_NAME where pendingSync is not null")
    suspend fun getPendingSyncBookmarks(): List<BookmarkLocal>

    @Query("delete from $TABLE_NAME where url = :url and pendingSync is not null")
    suspend fun deletePendingSyncBookmark(url: String)

    companion object {

        private const val SELECT_ALL_FROM_BOOKMARKS = "select $TABLE_NAME.* from $TABLE_NAME"

        private const val MATCH_TERM = "case " +
            "when :term = '' then 1 " +
            "else ($TABLE_NAME.rowid in (" +
            "select rowid from $FTS_TABLE_NAME where $FTS_TABLE_NAME match :term" +
            ") OR " +
            "$TABLE_NAME.url like '%' || :termNoFts || '%') " +
            "end"

        private const val LIKE_TERM = "case " +
            "when :term = '' then 1 " +
            "else ($TABLE_NAME.url like '%' || :term || '%' or " +
            "$TABLE_NAME.title like '%' || :term || '%' or " +
            "$TABLE_NAME.description like '%' || :term || '%' or " +
            "$TABLE_NAME.notes like '%' || :term || '%' or " +
            "$TABLE_NAME.websiteTitle like '%' || :term || '%' or " +
            "$TABLE_NAME.websiteDescription like '%' || :term || '%') " +
            "end"

        private const val MATCH_TAGS = "case " +
            "when :tag1 = '' then 1 " +
            "else $TABLE_NAME.rowid in (select rowid from $FTS_TABLE_NAME where tagNames match :tag1) " +
            "end " +
            "and case " +
            "when :tag2 = '' then 1 " +
            "else $TABLE_NAME.rowid in (select rowid from $FTS_TABLE_NAME where tagNames match :tag2) " +
            "end " +
            "and case " +
            "when :tag3 = '' then 1 " +
            "else $TABLE_NAME.rowid in (select rowid from $FTS_TABLE_NAME where tagNames match :tag3) " +
            "end "

        private const val LIKE_TAGS = "case " +
            "when :tag1 = '' then 1 " +
            "else $TABLE_NAME.tagNames like '%' || :tag1 || '%' " +
            "end " +
            "and case " +
            "when :tag2 = '' then 1 " +
            "else $TABLE_NAME.tagNames like '%' || :tag2 || '%' " +
            "end " +
            "and case " +
            "when :tag3 = '' then 1 " +
            "else $TABLE_NAME.tagNames like '%' || :tag3 || '%' " +
            "end "

        private const val MATCH_UNTAGGED_OR_TAGS = "case " +
            "when :untaggedOnly = 1 then tagNames = '' " +
            "else $MATCH_TAGS " +
            "end"

        private const val LIKE_UNTAGGED_OR_TAGS = "case " +
            "when :untaggedOnly = 1 then tagNames = '' " +
            "else $LIKE_TAGS " +
            "end"

        private const val WHERE_PUBLIC = "case " +
            "when :publicBookmarksOnly = 1 then shared = 1 " +
            "when :privateBookmarksOnly = 1 then shared = 0 " +
            "when :ignoreVisibility = 1 then 1 " +
            "end"

        private const val WHERE_READ_LATER = "case " +
            "when :readLaterOnly = 1 then unread = 1 " +
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
            "case when :sortType = 0 then dateAdded end DESC, " +
            "case when :sortType = 1 then dateAdded end ASC," +
            "case when :sortType = 2 then dateModified end DESC, " +
            "case when :sortType = 3 then dateModified end ASC," +
            "case when :sortType = 4 then title end ASC," +
            "case when :sortType = 5 then title end DESC"

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
