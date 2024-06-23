package com.fibelatti.pinboard.features.appstate

import com.fibelatti.bookmarking.test.MockDataProvider.createTag
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class SearchParametersTest {

    @Test
    fun `WHEN term is empty and tags is empty THEN isActive is false`() {
        assertThat(SearchParameters(term = "", tags = emptyList()).isActive()).isFalse()
    }

    @Test
    fun `WHEN term is not empty THEN isActive is true`() {
        assertThat(SearchParameters(term = "term", tags = emptyList()).isActive()).isTrue()
    }

    @Test
    fun `WHEN tags is not empty THEN isActive is true`() {
        assertThat(SearchParameters(term = "", tags = listOf(createTag())).isActive()).isTrue()
    }
}
