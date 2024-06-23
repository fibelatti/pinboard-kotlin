package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidateUrlTest {

    private val validateUrl = ValidateUrl()

    fun validUrls(): List<String> = mutableListOf<String>().apply {
        ValidUrlScheme.ALL_SCHEMES.forEach {
            add("$it://${com.fibelatti.bookmarking.test.MockDataProvider.MOCK_URL_INVALID}")
        }
        add("https://bit.ly")
        add("http://192.168.0.92/something")
    }

    fun invalidUrls(): List<String> = mutableListOf<String>().apply {
        add(com.fibelatti.bookmarking.test.MockDataProvider.MOCK_URL_INVALID)
        add("google")
        add("google com")
    }

    @ParameterizedTest
    @MethodSource("validUrls")
    fun `GIVEN that a valid url is received WHEN validateUrl is called THEN Success is returned`(url: String) =
        runTest {
            // WHEN
            val result = validateUrl(url)

            // THEN
            assertThat(result.getOrNull()).isEqualTo(url)
        }

    @ParameterizedTest
    @MethodSource("invalidUrls")
    fun `GIVEN that an invalid url is received WHEN validateUrl is called THEN Failure is returned`(
        invalidUrl: String,
    ) = runTest {
        // WHEN
        val result = validateUrl(invalidUrl)

        // THEN
        assertThat(result.exceptionOrNull()).isInstanceOf(InvalidUrlException::class.java)
    }
}
