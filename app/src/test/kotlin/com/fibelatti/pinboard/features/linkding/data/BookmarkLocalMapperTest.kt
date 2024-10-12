package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.pinboard.MockDataProvider.createBookmarkLocal
import com.fibelatti.pinboard.MockDataProvider.createPost
import com.fibelatti.pinboard.MockDataProvider.mockHash
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsResponse
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlNotes
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookmarkLocalMapperTest {

    private val dateAdded = "2019-01-10T08:20:10Z"
    private val dateModified = "2019-03-10T08:20:10Z"

    private val mapper = BookmarkLocalMapper(
        dateFormatter = mockk {
            every { dataFormatToDisplayFormat(any()) } answers { firstArg() }
        },
    )

    @Nested
    inner class MapTests {

        @Test
        fun `maps all fields`() {
            val archived = randomBoolean()
            val unread = randomBoolean()
            val shared = randomBoolean()

            val input = BookmarkLocal(
                id = mockHash,
                url = mockUrlValid,
                title = mockTitle,
                description = mockUrlDescription,
                notes = mockUrlNotes,
                isArchived = archived,
                unread = unread,
                shared = shared,
                tagNames = mockTagsResponse,
                dateAdded = dateAdded,
                dateModified = dateModified,
                pendingSync = null,
            )

            val expected = Post(
                url = mockUrlValid,
                title = mockTitle,
                description = mockUrlDescription,
                id = mockHash,
                dateAdded = dateAdded,
                dateModified = dateModified,
                private = !shared,
                readLater = unread,
                tags = mockTags,
                notes = mockUrlNotes,
                isArchived = archived,
                pendingSync = null,
            )

            assertThat(mapper.map(input)).isEqualTo(expected)
        }

        @Test
        fun `GIVEN pendingSync has value WHEN map is called THEN Post is returned with respective pendingSync`() {
            assertThat(mapper.map(createBookmarkLocal(pendingSync = PendingSyncDto.ADD)))
                .isEqualTo(createPost(pendingSync = PendingSync.ADD))
            assertThat(mapper.map(createBookmarkLocal(pendingSync = PendingSyncDto.UPDATE)))
                .isEqualTo(createPost(pendingSync = PendingSync.UPDATE))
            assertThat(mapper.map(createBookmarkLocal(pendingSync = PendingSyncDto.DELETE)))
                .isEqualTo(createPost(pendingSync = PendingSync.DELETE))
            assertThat(mapper.map(createBookmarkLocal(pendingSync = null)))
                .isEqualTo(createPost(pendingSync = null))
        }
    }

    @Nested
    inner class MapReverseTests {

        @Test
        fun `maps all fields`() {
            val archived = randomBoolean()
            val unread = randomBoolean()
            val shared = randomBoolean()

            val input = Post(
                url = mockUrlValid,
                title = mockTitle,
                description = mockUrlDescription,
                id = mockHash,
                dateAdded = dateAdded,
                dateModified = dateModified,
                private = !shared,
                readLater = unread,
                tags = mockTags,
                notes = mockUrlNotes,
                isArchived = archived,
                pendingSync = null,
            )

            val expected = BookmarkLocal(
                id = mockHash,
                url = mockUrlValid,
                title = mockTitle,
                description = mockUrlDescription,
                notes = mockUrlNotes,
                isArchived = archived,
                unread = unread,
                shared = shared,
                tagNames = mockTagsResponse,
                dateAdded = dateAdded,
                dateModified = dateModified,
                pendingSync = null,
            )

            assertThat(mapper.mapReverse(input)).isEqualTo(expected)
        }

        @Test
        fun `GIVEN pendingSync has value WHEN mapReverse is called THEN Post is returned with respective pendingSync`() {
            assertThat(mapper.mapReverse(createPost(pendingSync = PendingSync.ADD)))
                .isEqualTo(createBookmarkLocal(pendingSync = PendingSyncDto.ADD))
            assertThat(mapper.mapReverse(createPost(pendingSync = PendingSync.UPDATE)))
                .isEqualTo(createBookmarkLocal(pendingSync = PendingSyncDto.UPDATE))
            assertThat(mapper.mapReverse(createPost(pendingSync = PendingSync.DELETE)))
                .isEqualTo(createBookmarkLocal(pendingSync = PendingSyncDto.DELETE))
            assertThat(mapper.mapReverse(createPost(pendingSync = null)))
                .isEqualTo(createBookmarkLocal(pendingSync = null))
        }
    }
}
