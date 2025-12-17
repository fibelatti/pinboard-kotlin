package com.fibelatti.pinboard.features.export

import android.content.Context
import com.fibelatti.core.functional.UseCase
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.linkding.data.BookmarkLocalMapper
import com.fibelatti.pinboard.features.linkding.data.BookmarksDao
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.domain.model.Post
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.DL
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.dd
import kotlinx.html.dl
import kotlinx.html.dt
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import kotlinx.html.title
import timber.log.Timber

class ExportBookmarksUseCase @Inject constructor(
    @ApplicationContext context: Context,
    private val appModeProvider: AppModeProvider,
    private val postDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val bookmarksDao: BookmarksDao,
    private val bookmarksMapper: BookmarkLocalMapper,
    private val dateFormatter: DateFormatter,
) : UseCase<File?> {

    private val parentDir: File = context.cacheDir

    override suspend fun invoke(): File? = runCatching {
        withContext(Dispatchers.Default) {
            Timber.d("Loading bookmarks to export...")

            val posts: List<Post> = getPosts()
            Timber.d("${posts.size} bookmarks found.")

            return@withContext if (posts.isNotEmpty()) {
                createExportFile().apply {
                    exportBookmarks(file = this, posts = posts)
                }
            } else {
                null
            }
        }
    }.getOrNull()

    private suspend fun getPosts(): List<Post> {
        val appMode: AppMode = appModeProvider.appMode.value
        Timber.d("App mode: $appMode")

        return if (appMode == AppMode.PINBOARD) {
            postDao.getAllPosts().let(postDtoMapper::mapList)
        } else {
            bookmarksDao.getAllBookmarks().let(bookmarksMapper::mapList)
        }
    }

    private suspend fun createExportFile(): File = withContext(Dispatchers.IO) {
        Timber.d("Creating export file...")

        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file: File = File("$parentDir/bookmarks_$timestamp.html").apply {
            if (exists()) delete()
            createNewFile()
        }

        return@withContext file
    }

    private suspend fun exportBookmarks(file: File, posts: List<Post>) {
        val sw = StringWriter()

        sw.appendLine("<!DOCTYPE netscape-bookmark-file-1>")
        sw.appendHTML().html {
            head {
                meta {
                    httpEquiv = "Content-Type"
                    content = "text/html; charset=UTF-8"
                }
                title("Bookmarks")
            }
            body {
                h1 {
                    text("Bookmarks")
                }
                dl {
                    p()

                    for (item: Post in posts) {
                        writePostToHtml(item)
                    }
                }

                p()
            }
        }

        withContext(Dispatchers.IO) {
            file.writeText(sw.toString())
        }
    }

    private fun DL.writePostToHtml(item: Post) {
        dt {
            a(href = item.url) {
                attributes["add_date"] = dateFormatter.dataFormatToEpoch(item.dateAdded).toString()

                if (item.dateModified != item.dateAdded) {
                    attributes["last_modified"] = dateFormatter.dataFormatToEpoch(item.dateModified).toString()
                }

                attributes["private"] = if (item.private == true) "1" else "0"
                attributes["toread"] = if (item.readLater == true) "1" else "0"

                if (!item.tags.isNullOrEmpty()) {
                    attributes["tags"] = item.tags.joinToString(separator = ",") { it.name }
                }

                text(item.title)
            }
        }

        if (item.description.isNotBlank() || !item.notes.isNullOrBlank()) {
            dd {
                text(
                    buildString {
                        append(item.description)

                        if (!item.notes.isNullOrBlank()) {
                            append("[linkding-notes]")
                            append(item.notes)
                            append("[/linkding-notes]")
                        }
                    },
                )
            }
        }
    }
}
