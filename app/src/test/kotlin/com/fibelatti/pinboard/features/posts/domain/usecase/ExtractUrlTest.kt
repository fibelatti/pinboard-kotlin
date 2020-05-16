package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.test.extension.shouldBe
import com.fibelatti.core.test.extension.shouldBeAnInstanceOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExtractUrlTest {

    private val extractUrl = ExtractUrl()

    @ParameterizedTest
    @MethodSource("testCases")
    fun `WHEN extractUrl is called THEN a valid url should be returned if it can be found`(scenario: Pair<String, Result<String>>) {
        // GIVEN
        val (input, expectedResult) = scenario

        // WHEN
        val result = runBlocking { extractUrl(input) }

        // THEN
        if (result is Success) {
            result shouldBe expectedResult
        } else {
            result.exceptionOrNull()?.shouldBeAnInstanceOf<InvalidUrlException>()
                ?: fail("The expected error was not received")
        }
    }

    fun testCases(): List<Pair<String, Result<String>>> =
        mutableListOf<Pair<String, Result<String>>>().apply {
            ValidUrlScheme.ALL_SCHEMES.map {
                add("Check this awesome url $it://www.url.com" to Success("$it://www.url.com"))
            }
            add("https://web.archive.org/web/20111117040806/http://www.url.com" to Success("https://web.archive.org/web/20111117040806/http://www.url.com"))
            add("Check this not so awesome url www.url.com" to Failure(InvalidUrlException()))
        }
}
