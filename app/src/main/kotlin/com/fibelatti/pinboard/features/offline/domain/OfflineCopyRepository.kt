package com.fibelatti.pinboard.features.offline.domain

import com.fibelatti.core.functional.coRunCatching
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.offline.OfflineCopyStorage
import com.fibelatti.pinboard.features.offline.data.OfflineCopiesDao
import com.fibelatti.pinboard.features.offline.data.OfflineCopyDto
import com.fibelatti.pinboard.features.offline.data.OfflineCopyDtoMapper
import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single owner of the row-and-file pair that makes up an offline copy.
 *
 * Everything that creates or destroys a copy goes through here, so the database row and the file on
 * disk can never drift apart.
 *
 * Every method takes the [AppMode] it should act on rather than reading it from
 * [com.fibelatti.pinboard.core.AppModeProvider]. Resolving it here would mean a mode switch between
 * the start and end of an operation silently moved a copy to the wrong account.
 */
@Singleton
class OfflineCopyRepository @Inject constructor(
    private val offlineCopiesDao: OfflineCopiesDao,
    private val offlineCopyStorage: OfflineCopyStorage,
    private val offlineCopyDtoMapper: OfflineCopyDtoMapper,
    private val dateFormatter: DateFormatter,
) {

    fun getOfflineCopies(appMode: AppMode): Flow<List<OfflineCopy>> =
        offlineCopiesDao.getOfflineCopies(appMode = appMode.name)
            .map(offlineCopyDtoMapper::mapList)

    suspend fun getOfflineCopy(appMode: AppMode, bookmarkId: String): OfflineCopy? {
        val dto: OfflineCopyDto = offlineCopiesDao.getOfflineCopy(appMode = appMode.name, bookmarkId = bookmarkId)
            ?: return null

        // A row without its file is unusable, and keeping it would make the UI offer a copy that
        // can't be opened. Drop it instead of returning it.
        if (!offlineCopyStorage.file(appMode = appMode, bookmarkId = bookmarkId).exists()) {
            offlineCopiesDao.deleteOfflineCopy(appMode = appMode.name, bookmarkId = bookmarkId)
            return null
        }

        return offlineCopyDtoMapper.map(dto)
    }

    fun fileFor(offlineCopy: OfflineCopy): File = offlineCopyStorage.file(
        appMode = offlineCopy.appMode,
        bookmarkId = offlineCopy.bookmarkId,
    )

    suspend fun save(
        appMode: AppMode,
        bookmarkId: String,
        url: String,
        title: String,
        html: String,
        truncated: Boolean,
    ): Result<OfflineCopy> = coRunCatching {
        val file: File = offlineCopyStorage.write(
            appMode = appMode,
            bookmarkId = bookmarkId,
            content = html,
        )

        val dto = OfflineCopyDto(
            bookmarkId = bookmarkId,
            appMode = appMode.name,
            url = url,
            title = title,
            fileName = file.name,
            sizeBytes = file.length(),
            dateCreated = dateFormatter.nowAsDataFormat(),
            truncated = truncated,
        )

        offlineCopiesDao.saveOfflineCopy(dto)

        offlineCopyDtoMapper.map(dto)
    }

    suspend fun delete(appMode: AppMode, bookmarkId: String) {
        offlineCopiesDao.deleteOfflineCopy(appMode = appMode.name, bookmarkId = bookmarkId)
        offlineCopyStorage.delete(appMode = appMode, bookmarkId = bookmarkId)
    }

    suspend fun deleteAll(appMode: AppMode) {
        offlineCopiesDao.deleteAllOfflineCopies(appMode = appMode.name)
        offlineCopyStorage.deleteAll(appMode = appMode)
    }

    suspend fun deleteEverything() {
        for (dto in offlineCopiesDao.getEveryOfflineCopy()) {
            offlineCopiesDao.deleteOfflineCopy(appMode = dto.appMode, bookmarkId = dto.bookmarkId)
        }
        offlineCopyStorage.deleteEverything()
    }

    /**
     * Drops copies whose bookmark no longer exists, then any file left without a row.
     *
     * A full sync deletes and reinserts bookmark rows, so copies are expected to become orphaned.
     * Without this, files would accumulate forever.
     */
    suspend fun deleteOrphaned(appMode: AppMode) {
        val orphanedIds: List<String> = when (appMode) {
            // Both modes share the same table
            AppMode.PINBOARD, AppMode.NO_API -> offlineCopiesDao.getOrphanedPinboardCopyIds(appMode = appMode.name)

            AppMode.LINKDING -> offlineCopiesDao.getOrphanedLinkdingCopyIds(appMode = appMode.name)

            AppMode.UNSET -> return
        }

        for (bookmarkId in orphanedIds) {
            offlineCopiesDao.deleteOfflineCopy(appMode = appMode.name, bookmarkId = bookmarkId)
            offlineCopyStorage.delete(appMode = appMode, bookmarkId = bookmarkId)
        }

        offlineCopyStorage.deleteFilesNotIn(
            appMode = appMode,
            fileNames = offlineCopiesDao.getAllOfflineCopies(appMode = appMode.name)
                .mapTo(mutableSetOf(), OfflineCopyDto::fileName),
        )
    }

    /**
     * The space taken by every account's copies, measured on disk rather than summed from the rows.
     *
     * This backs the "clear all offline copies" preference, which deletes the directory outright
     * so it has to account for files that no longer have a row as well.
     */
    suspend fun totalSizeOnDisk(): Long = offlineCopyStorage.totalSize()
}
