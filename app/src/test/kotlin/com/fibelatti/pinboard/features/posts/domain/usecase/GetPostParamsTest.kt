package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.pinboard.core.AppConfig.DEFAULT_PAGE_SIZE
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class GetPostParamsTest {

    @Test
    fun `GetPostParams default values should be set`() {
        val params = GetPostParams()

        assertThat(params.sorting).isEqualTo(ByDateAddedNewestFirst)
        assertThat(params.searchTerm).isEmpty()
        assertThat(params.tags).isEqualTo(GetPostParams.Tags.None)
        assertThat(params.visibility).isEqualTo(PostVisibility.None)
        assertThat(params.readLater).isFalse()
        assertThat(params.limit).isEqualTo(DEFAULT_PAGE_SIZE)
        assertThat(params.offset).isEqualTo(0)
    }
}
