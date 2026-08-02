package com.fibelatti.pinboard.features.offline.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocal
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineCopiesDao {

    @Query("select * from ${OfflineCopyDto.TABLE_NAME} where appMode = :appMode order by dateCreated desc")
    fun getOfflineCopies(appMode: String): Flow<List<OfflineCopyDto>>

    @Query("select * from ${OfflineCopyDto.TABLE_NAME} where appMode = :appMode and bookmarkId = :bookmarkId")
    suspend fun getOfflineCopy(appMode: String, bookmarkId: String): OfflineCopyDto?

    @Query("select * from ${OfflineCopyDto.TABLE_NAME} where appMode = :appMode")
    suspend fun getAllOfflineCopies(appMode: String): List<OfflineCopyDto>

    @Query("select * from ${OfflineCopyDto.TABLE_NAME}")
    suspend fun getEveryOfflineCopy(): List<OfflineCopyDto>

    /**
     * The ids of copies whose bookmark is no longer in the Pinboard table. Used to clean up after a
     * sync deletes bookmarks out from under them.
     */
    @Query(
        """
        select bookmarkId from ${OfflineCopyDto.TABLE_NAME}
        where appMode = :appMode and bookmarkId not in (select hash from $POST_TABLE_NAME)
        """,
    )
    suspend fun getOrphanedPinboardCopyIds(appMode: String): List<String>

    /**
     * The ids of copies whose bookmark is no longer in the Linkding table. Used to clean up after a
     * sync deletes bookmarks out from under them.
     */
    @Query(
        """
        select bookmarkId from ${OfflineCopyDto.TABLE_NAME}
        where appMode = :appMode and bookmarkId not in (select id from ${BookmarkLocal.TABLE_NAME})
        """,
    )
    suspend fun getOrphanedLinkdingCopyIds(appMode: String): List<String>

    @Upsert
    suspend fun saveOfflineCopy(offlineCopyDto: OfflineCopyDto)

    @Query("delete from ${OfflineCopyDto.TABLE_NAME} where appMode = :appMode and bookmarkId = :bookmarkId")
    suspend fun deleteOfflineCopy(appMode: String, bookmarkId: String)

    @Query("delete from ${OfflineCopyDto.TABLE_NAME} where appMode = :appMode")
    suspend fun deleteAllOfflineCopies(appMode: String)
}
