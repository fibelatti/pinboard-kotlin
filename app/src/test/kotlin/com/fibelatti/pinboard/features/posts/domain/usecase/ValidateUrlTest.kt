package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.test.extension.callSuspend
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import com.fibelatti.pinboard.MockDataProvider
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidateUrlTest {

    private val validateUrl = ValidateUrl()

    fun validUrls(): List<String> = mutableListOf<String>().apply {
        UrlValidSchemes.allSchemes().forEach {
            add("$it://${MockDataProvider.mockUrlInvalid}")
        }
        add("https://bit.ly")
    }

    fun invalidUrls(): List<String> = mutableListOf<String>().apply {
        add(MockDataProvider.mockUrlInvalid)
        add("google")
        add("google com")
    }

    @ParameterizedTest
    @MethodSource("validUrls")
    fun `GIVEN that a valid url is received WHEN validateUrl is called THEN Success is returned`(url: String) {
        // WHEN
        val result = callSuspend { validateUrl(url) }

        // THEN
        result.shouldBeAnInstanceOf<Success<Unit>>()
        result.getOrNull() shouldBe url
    }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    fun `GIVEN that an invalid url is received WHEN validateUrl is called THEN Failure is returned`(invalidUrl: String) {
        // WHEN
        val result = callSuspend { validateUrl(invalidUrl) }

        // THEN
        result.shouldBeAnInstanceOf<Failure>()
        result.exceptionOrNull()?.shouldBeAnInstanceOf<InvalidUrlException>()
    }
}
