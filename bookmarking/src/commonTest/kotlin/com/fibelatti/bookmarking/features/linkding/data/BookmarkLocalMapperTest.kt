package com.fibelatti.bookmarking.features.linkding.data

import com.fibelatti.bookmarking.MockDataProvider.MOCK_HASH
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TAGS
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TAGS_RESPONSE
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TIME
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TITLE
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_DESCRIPTION
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_NOTES
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_VALID
import com.fibelatti.bookmarking.MockDataProvider.createBookmarkLocal
import com.fibelatti.bookmarking.MockDataProvider.createPost
import com.fibelatti.bookmarking.features.posts.data.model.PendingSyncDto
import com.fibelatti.bookmarking.features.posts.domain.model.PendingSync
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.linkding.data.BookmarkLocal
import com.fibelatti.bookmarking.linkding.data.BookmarkLocalMapper
import com.fibelatti.bookmarking.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookmarkLocalMapperTest {

    private val mapper = BookmarkLocalMapper(
        dateFormatter = mockk {
            every { tzFormatToDisplayFormat(any()) } answers { invocation.args[0] as String }
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
                id = MOCK_HASH,
                url = MOCK_URL_VALID,
                title = MOCK_TITLE,
                description = MOCK_URL_DESCRIPTION,
                notes = MOCK_URL_NOTES,
                isArchived = archived,
                unread = unread,
                shared = shared,
                tagNames = MOCK_TAGS_RESPONSE,
                dateModified = MOCK_TIME,
                pendingSync = null,
            )

            val expected = Post(
                url = MOCK_URL_VALID,
                title = MOCK_TITLE,
                description = MOCK_URL_DESCRIPTION,
                id = MOCK_HASH,
                time = MOCK_TIME,
                formattedTime = MOCK_TIME,
                private = !shared,
                readLater = unread,
                tags = MOCK_TAGS,
                notes = MOCK_URL_NOTES,
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
                url = MOCK_URL_VALID,
                title = MOCK_TITLE,
                description = MOCK_URL_DESCRIPTION,
                id = MOCK_HASH,
                time = MOCK_TIME,
                formattedTime = MOCK_TIME,
                private = !shared,
                readLater = unread,
                tags = MOCK_TAGS,
                notes = MOCK_URL_NOTES,
                isArchived = archived,
                pendingSync = null,
            )

            val expected = BookmarkLocal(
                id = MOCK_HASH,
                url = MOCK_URL_VALID,
                title = MOCK_TITLE,
                description = MOCK_URL_DESCRIPTION,
                notes = MOCK_URL_NOTES,
                isArchived = archived,
                unread = unread,
                shared = shared,
                tagNames = MOCK_TAGS_RESPONSE,
                dateModified = MOCK_TIME,
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
