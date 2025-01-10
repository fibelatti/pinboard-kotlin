package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAGS
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_TAG_VALUES
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_DESCRIPTION
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_NOTES
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_TITLE
import com.fibelatti.pinboard.MockDataProvider.SAMPLE_URL_VALID
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
            url = SAMPLE_URL_VALID,
            title = SAMPLE_URL_TITLE,
            description = SAMPLE_URL_DESCRIPTION,
            notes = SAMPLE_URL_NOTES,
            isArchived = archived,
            unread = unread,
            shared = shared,
            tagNames = SAMPLE_TAG_VALUES,
            dateAdded = dateAdded,
            dateModified = dateModified,
        )

        val expected = Post(
            url = SAMPLE_URL_VALID,
            title = SAMPLE_URL_TITLE,
            description = SAMPLE_URL_DESCRIPTION,
            id = "1",
            dateAdded = dateAdded,
            dateModified = dateModified,
            private = !shared,
            readLater = unread,
            tags = SAMPLE_TAGS,
            notes = SAMPLE_URL_NOTES,
            isArchived = archived,
        )

        assertThat(mapper.map(input)).isEqualTo(expected)
    }

    @Test
    fun `should use fallback values`() {
        val input = BookmarkRemote(
            id = 1,
            url = SAMPLE_URL_VALID,
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
            url = SAMPLE_URL_VALID,
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
