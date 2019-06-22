package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.MockDataProvider.createTag
import org.junit.jupiter.api.Test

internal class SearchParametersTest {

    @Test
    fun `WHEN term is empty and tags is empty THEN isActive is false`() {
        SearchParameters(term = "", tags = emptyList()).isActive() shouldBe false
    }

    @Test
    fun `WHEN term is not empty THEN isActive is true`() {
        SearchParameters(term = "term", tags = emptyList()).isActive() shouldBe true
    }

    @Test
    fun `WHEN tags is not empty THEN isActive is true`() {
        SearchParameters(term = "", tags = listOf(createTag())).isActive() shouldBe true
    }
}
