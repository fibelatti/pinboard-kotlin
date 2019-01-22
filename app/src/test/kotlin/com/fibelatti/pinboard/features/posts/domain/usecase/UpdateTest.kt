package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider.mockTime
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import org.junit.jupiter.api.Test

class UpdateTest {

    private val mockPostsRepository = mock<PostsRepository>()

    private val update = Update(mockPostsRepository)

    @Test
    fun `GIVEN repository fails WHEN Update is called THEN Failure is returned`() {
        // GIVEN
        givenSuspend { mockPostsRepository.update() }
            .willReturn(Failure(Exception()))

        // WHEN
        val result = callSuspend { update() }

        // THEN
        result.shouldBeAnInstanceOf<Failure>()
        result.exceptionOrNull()?.shouldBeAnInstanceOf<Exception>()
    }

    @Test
    fun `GIVEN repository succeeds WHEN Update is called THEN Failure is returned`() {
        // GIVEN
        givenSuspend { mockPostsRepository.update() }
            .willReturn(Success(mockTime))

        // WHEN
        val result = callSuspend { update() }

        // THEN
        result.shouldBeAnInstanceOf<Success<String>>()
        result.getOrNull() shouldBe mockTime
    }
}
