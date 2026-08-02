package com.fibelatti.pinboard.features.offline

import android.content.Context
import com.fibelatti.pinboard.core.AppMode
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class OfflineCopyStorageTest {

    @TempDir
    private lateinit var tempDir: File

    private val context: Context = mockk {
        every { filesDir } answers { tempDir }
    }

    private val storage: OfflineCopyStorage by lazy {
        OfflineCopyStorage(
            context = context,
            ioDispatcher = Dispatchers.Unconfined,
        )
    }

    @Test
    fun `WHEN write is called THEN the content is stored and no temp file is left behind`() = runTest {
        val file = storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "<html>hi</html>")

        assertThat(file.readText()).isEqualTo("<html>hi</html>")
        assertThat(file.name).isEqualTo("abc.html")
        assertThat(file.parentFile?.listFiles()?.map { it.name }).containsExactly("abc.html")
    }

    @Test
    fun `WHEN write replaces an existing copy THEN the new content wins`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "old")
        val file = storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "new")

        assertThat(file.readText()).isEqualTo("new")
        assertThat(file.parentFile?.listFiles()?.map { it.name }).containsExactly("abc.html")
    }

    @Test
    fun `WHEN copies exist for different app modes THEN they do not collide`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "pinboard")
        storage.write(appMode = AppMode.LINKDING, bookmarkId = "abc", content = "linkding")

        assertThat(storage.file(AppMode.PINBOARD, "abc").readText()).isEqualTo("pinboard")
        assertThat(storage.file(AppMode.LINKDING, "abc").readText()).isEqualTo("linkding")
    }

    @Test
    fun `WHEN delete is called THEN only that copy is removed`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "a")
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "def", content = "b")

        storage.delete(appMode = AppMode.PINBOARD, bookmarkId = "abc")

        assertThat(storage.file(AppMode.PINBOARD, "abc").exists()).isFalse()
        assertThat(storage.file(AppMode.PINBOARD, "def").exists()).isTrue()
    }

    @Test
    fun `WHEN deleteAll is called THEN only that app mode is cleared`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "a")
        storage.write(appMode = AppMode.LINKDING, bookmarkId = "def", content = "b")

        storage.deleteAll(appMode = AppMode.PINBOARD)

        assertThat(storage.file(AppMode.PINBOARD, "abc").exists()).isFalse()
        assertThat(storage.file(AppMode.LINKDING, "def").exists()).isTrue()
    }

    @Test
    fun `WHEN deleteEverything is called THEN nothing is left`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "a")
        storage.write(appMode = AppMode.LINKDING, bookmarkId = "def", content = "b")

        storage.deleteEverything()

        assertThat(storage.totalSize()).isEqualTo(0)
    }

    @Test
    fun `WHEN deleteFilesNotIn is called THEN files without a matching row are removed`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "keep", content = "a")
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "drop", content = "b")

        storage.deleteFilesNotIn(appMode = AppMode.PINBOARD, fileNames = setOf("keep.html"))

        assertThat(storage.file(AppMode.PINBOARD, "keep").exists()).isTrue()
        assertThat(storage.file(AppMode.PINBOARD, "drop").exists()).isFalse()
    }

    @Test
    fun `WHEN totalSize is called THEN it sums every app mode`() = runTest {
        storage.write(appMode = AppMode.PINBOARD, bookmarkId = "abc", content = "12345")
        storage.write(appMode = AppMode.LINKDING, bookmarkId = "def", content = "123")

        assertThat(storage.totalSize()).isEqualTo(8)
    }

    @Test
    fun `WHEN nothing was ever saved THEN totalSize is zero rather than failing`() = runTest {
        assertThat(storage.totalSize()).isEqualTo(0)
    }
}
