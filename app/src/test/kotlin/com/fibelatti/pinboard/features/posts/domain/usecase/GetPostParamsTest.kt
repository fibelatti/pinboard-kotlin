package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.pinboard.features.appstate.NewestFirst
import org.junit.jupiter.api.Test

internal class GetPostParamsTest {

    @Test
    fun `GetPostParams default values should be set`() {
        val params = GetPostParams()

        params.sorting shouldBe NewestFirst
        params.searchTerm shouldBe ""
        params.tagParams shouldBe GetPostParams.Tags.None
        params.visibilityParams shouldBe GetPostParams.Visibility.None
        params.readLater shouldBe false
        params.limit shouldBe -1
    }
}
