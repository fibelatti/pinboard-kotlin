package com.fibelatti.pinboard.features.offline

import android.content.Context
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Owns the files backing the offline copies.
 */
@Singleton
class OfflineCopyStorage @Inject constructor(
    @ApplicationContext context: Context,
    @Scope(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val rootDir: File by lazy(LazyThreadSafetyMode.NONE) {
        File(context.filesDir, ROOT_DIR_NAME)
    }

    private fun fileName(bookmarkId: String): String = "$bookmarkId.html"

    private fun dir(appMode: AppMode): File = File(rootDir, appMode.name)

    fun file(appMode: AppMode, bookmarkId: String): File = File(dir(appMode), fileName(bookmarkId))

    /**
     * Writes [content] and returns the resulting file.
     *
     * The content goes to a temporary sibling that is only renamed into place once it is fully
     * written, so a process death mid-write can never leave behind a truncated file that looks
     * complete to every later reader.
     */
    suspend fun write(appMode: AppMode, bookmarkId: String, content: String): File {
        return withContext(ioDispatcher) {
            val dir: File = dir(appMode).apply { mkdirs() }
            val target = File(dir, fileName(bookmarkId))
            val temp = File(dir, "${fileName(bookmarkId)}$TEMP_SUFFIX")

            try {
                temp.writeText(content)
                // An atomic replacement, so a failed refresh can never leave the user with neither the
                // old copy nor the new one.
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } finally {
                temp.delete()
            }

            target
        }
    }

    suspend fun delete(appMode: AppMode, bookmarkId: String) {
        withContext(ioDispatcher) {
            file(appMode = appMode, bookmarkId = bookmarkId).delete()
        }
    }

    suspend fun deleteAll(appMode: AppMode) {
        withContext(ioDispatcher) {
            dir(appMode).deleteRecursively()
        }
    }

    suspend fun deleteEverything() {
        withContext(ioDispatcher) {
            rootDir.deleteRecursively()
        }
    }

    suspend fun totalSize(): Long {
        return withContext(ioDispatcher) {
            rootDir.walkBottomUp().filter(File::isFile).sumOf(File::length)
        }
    }

    /**
     * Removes files that no longer have a matching database row. Guards against the case where a
     * row was deleted but the file write survived, which would otherwise leak disk space silently.
     */
    suspend fun deleteFilesNotIn(appMode: AppMode, fileNames: Set<String>) {
        withContext(ioDispatcher) {
            dir(appMode).listFiles()
                ?.filter { it.isFile && it.name !in fileNames }
                ?.forEach(File::delete)
        }
    }

    private companion object {

        const val ROOT_DIR_NAME = "offline"
        const val TEMP_SUFFIX = ".tmp"
    }
}
