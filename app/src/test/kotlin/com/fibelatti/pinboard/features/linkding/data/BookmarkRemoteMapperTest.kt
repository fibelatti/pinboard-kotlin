package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.MockDataProvider.mockTagsString
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.MockDataProvider.mockTitle
import com.fibelatti.pinboard.MockDataProvider.mockUrlDescription
import com.fibelatti.pinboard.MockDataProvider.mockUrlNotes
import com.fibelatti.pinboard.MockDataProvider.mockUrlValid
import com.fibelatti.pinboard.randomBoolean
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
            url = mockUrlValid,
            title = mockTitle,
            description = mockUrlDescription,
            notes = mockUrlNotes,
            isArchived = archived,
            unread = unread,
            shared = shared,
            tagNames = mockTagsString,
            dateModified = time,
        )

        val expected = Post(
            url = mockUrlValid,
            title = mockTitle,
            description = mockUrlDescription,
            id = "1",
            time = mockTime,
            formattedTime = mockTime,
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
            dateAdded = time,
            dateModified = null,
        )

        val expected = Post(
            url = mockUrlValid,
            title = "",
            description = "",
            id = "1",
            time = mockTime,
            formattedTime = mockTime,
            private = false,
            readLater = false,
            tags = null,
            notes = null,
            isArchived = null,
        )

        assertThat(mapper.map(input)).isEqualTo(expected)
    }
}
