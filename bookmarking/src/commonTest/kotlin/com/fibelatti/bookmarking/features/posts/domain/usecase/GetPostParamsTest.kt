package com.fibelatti.bookmarking.features.posts.domain.usecase

import com.fibelatti.bookmarking.core.Config.LOCAL_PAGE_SIZE
import com.fibelatti.bookmarking.features.appstate.NewestFirst
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class GetPostParamsTest {

    @Test
    fun `GetPostParams default values should be set`() {
        val params = GetPostParams()

        assertThat(params.sorting).isEqualTo(NewestFirst)
        assertThat(params.searchTerm).isEmpty()
        assertThat(params.tags).isEqualTo(GetPostParams.Tags.None)
        assertThat(params.visibility).isEqualTo(PostVisibility.None)
        assertThat(params.readLater).isFalse()
        assertThat(params.limit).isEqualTo(LOCAL_PAGE_SIZE)
        assertThat(params.offset).isEqualTo(0)
    }
}
