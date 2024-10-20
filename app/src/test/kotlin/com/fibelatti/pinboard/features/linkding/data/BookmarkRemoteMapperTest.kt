package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsString
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlNotes
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.randomBoolean
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class BookmarkRemoteMapperTest {

    private val dateAdded = "2019-01-10T08:20:10.123Z"
    private val dateModified = "2019-03-10T08:20:10.123Z"

    private val mapper = BookmarkRemoteMapper(
        dateFormatter = mockk {
            every { dataFormatToDisplayFormat(any()) } answers { firstArg() }
        },
    )

    @Test
    fun `should map all values`() {
        val archived = randomBoolean()
        val unread = randomBoolean()
        val shared = randomBoolean()

        val input = BookmarkRemote(
            id = 1,
            url = mockUrlValid,
            title = mockTitle,
            description = mockUrlDescription,
            notes = mockUrlNotes,
            isArchived = archived,
            unread = unread,
            shared = shared,
            tagNames = mockTagsString,
            dateAdded = dateAdded,
            dateModified = dateModified,
        )

        val expected = Post(
            url = mockUrlValid,
            title = mockTitle,
            description = mockUrlDescription,
            id = "1",
            dateAdded = dateAdded,
            dateModified = dateModified,
            private = !shared,
            readLater = unread,
            tags = mockTags,
            notes = mockUrlNotes,
            isArchived = archived,
        )

        assertThat(mapper.map(input)).isEqualTo(expected)
    }

    @Test
    fun `should use fallback values`() {
        val input = BookmarkRemote(
            id = 1,
            url = mockUrlValid,
            title = null,
            description = null,
            notes = null,
            isArchived = null,
            unread = null,
            shared = null,
            tagNames = null,
            dateAdded = "",
            dateModified = null,
        )

        val expected = Post(
            url = mockUrlValid,
            title = "",
            description = "",
            id = "1",
            dateAdded = "",
            dateModified = "",
            private = false,
            readLater = false,
            tags = null,
            notes = null,
            isArchived = null,
        )

        assertThat(mapper.map(input)).isEqualTo(expected)
    }
}
