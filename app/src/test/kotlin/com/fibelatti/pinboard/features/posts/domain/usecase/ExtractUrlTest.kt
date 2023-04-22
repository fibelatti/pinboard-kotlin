package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.exceptionOrNull
import com.fibelatti.core.functional.getOrNull
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExtractUrlTest {

    private val extractUrl = ExtractUrl()

    @Test
    fun `WHEN extractUrl is called with a highlighted text THEN a valid url and the text should be returned`() =
        runTest {
            // GIVEN
            val input = "\"Here's some cool thing for you.\n\nI think you'll like it!\"\nhttps://www.some-url.com"
            val expectedResult = ExtractUrl.ExtractedUrl(
                url = "https://www.some-url.com",
                highlightedText = "Here's some cool thing for you.\n\nI think you'll like it!",
            )

            // WHEN
            val result = extractUrl(input)

            // THEN
            assertThat(result.getOrNull()).isEqualTo(expectedResult)
        }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `WHEN extractUrl is called THEN a valid url should be returned if it can be found`(
        scenario: Pair<String, Result<String>>,
    ) = runTest {
        // GIVEN
        val (input, expectedResult) = scenario

        // WHEN
        val result = extractUrl(input)

        // THEN
        if (result is Success) {
            assertThat(result).isEqualTo(expectedResult)
        } else {
            assertThat(result.exceptionOrNull()).isInstanceOf(InvalidUrlException::class.java)
        }
    }

    fun testCases(): List<Pair<String, Result<ExtractUrl.ExtractedUrl>>> = buildList {
        ValidUrlScheme.ALL_SCHEMES.map {
            add("Check this awesome url $it://www.url.com" to Success(ExtractUrl.ExtractedUrl("$it://www.url.com")))
        }
        add(
            "https://web.archive.org/web/20111117040806/http://www.url.com" to
                Success(ExtractUrl.ExtractedUrl("https://web.archive.org/web/20111117040806/http://www.url.com")),
        )
        add("Check this not so awesome url www.url.com" to Failure(InvalidUrlException()))
    }
}
