package com.fibelatti.bookmarking.features.linkding.data

import com.fibelatti.bookmarking.MockDataProvider.MOCK_TAGS
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TAGS_STRING
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TIME
import com.fibelatti.bookmarking.MockDataProvider.MOCK_TITLE
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_DESCRIPTION
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_NOTES
import com.fibelatti.bookmarking.MockDataProvider.MOCK_URL_VALID
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.linkding.data.BookmarkRemote
import com.fibelatti.bookmarking.linkding.data.BookmarkRemoteMapper
import com.fibelatti.bookmarking.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class BookmarkRemoteMapperTest {

    private val mapper = BookmarkRemoteMapper(
        dateFormatter = mockk {
            every { tzFormatToDisplayFormat(any()) } answers { invocation.args[0] as String }
        },
    )

    private val time = "2019-01-10T08:20:10.123Z"

    @Test
    fun `should map all values`() {
        val archived = randomBoolean()
        val unread = randomBoolean()
        val shared = randomBoolean()

        val input = BookmarkRemote(
            id = 1,
            url = MOCK_URL_VALID,
            title = MOCK_TITLE,
            description = MOCK_URL_DESCRIPTION,
            notes = MOCK_URL_NOTES,
            isArchived = archived,
            unread = unread,
            shared = shared,
            tagNames = MOCK_TAGS_STRING,
            dateModified = time,
        )

        val expected = Post(
            url = MOCK_URL_VALID,
            title = MOCK_TITLE,
            description = MOCK_URL_DESCRIPTION,
            id = "1",
            time = MOCK_TIME,
            formattedTime = MOCK_TIME,
            private = !shared,
            readLater = unread,
            tags = MOCK_TAGS,
            notes = MOCK_URL_NOTES,
            isArchived = archived,
        )

        assertThat(mapper.map(input)).isEqualTo(expected)
    }

    @Test
    fun `should use fallback values`() {
        val input = BookmarkRemote(
            id = 1,
            url = MOCK_URL_VALID,
            title = null,
            description = null,
            notes = null,
            isArchived = null,
            unread = null,
            shared = null,
            tagNames = null,
            dateAdded = time,
            dateModified = null,
        )

        val expected = Post(
            url = MOCK_URL_VALID,
            title = "",
            description = "",
            id = "1",
            time = MOCK_TIME,
            formattedTime = MOCK_TIME,
            private = false,
            readLater = false,
            tags = null,
            notes = null,
            isArchived = null,
        )

        assertThat(mapper.map(input)).isEqualTo(expected)
    }
}
