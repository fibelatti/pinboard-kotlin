package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test

internal class PostsDaoUnitTest {

    @Test
    fun `GIVEN term is blank WHEN preFormatTerm is called THEN empty string is returned`() {
        PostsDao.preFormatTerm(" ") shouldBe ""
    }

    @Test
    fun `WHEN preFormatTerm is called THEN term formatted for the query is returned`() {
        PostsDao.preFormatTerm("term") shouldBe "href: \"term*\" OR description: \"term*\" OR extended: \"term*\""
    }

    @Test
    fun `WHEN preFormatTag is called THEN tag formatted for the query is returned`() {
        PostsDao.preFormatTag("tag") shouldBe "\"tag*\""
    }
}
