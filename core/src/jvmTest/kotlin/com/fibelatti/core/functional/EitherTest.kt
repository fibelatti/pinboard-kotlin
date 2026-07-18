package com.fibelatti.core.functional

import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class EitherTest {

    private val mockValue = true
    private val mockError = mockk<Exception>()

    private val right: Either<Throwable, Boolean> = Either.Right(mockValue)
    private val left: Either<Throwable, Boolean> = Either.Left(mockError)

    private val mockFnR = mockk<(Boolean) -> Unit>(relaxed = true)

    private val mockFnL = mockk<(Throwable) -> Unit>(relaxed = true)

    @BeforeEach
    fun setup() {
        clearMocks(mockFnR, mockFnL)
    }

    @Nested
    inner class LeftTests {

        @Test
        fun `GIVEN Either is Left AND either is called THEN fnL is invoked`() {
            // WHEN
            left.either(mockFnL, mockFnR)

            // THEN
            verify { mockFnL.invoke(mockError) }
            verify { mockFnR wasNot Called }
        }

        @Test
        fun `GIVEN Either is Left AND leftOrNull is called THEN left value is returned`() {
            assertThat(left.leftOrNull()).isEqualTo(mockError)
        }

        @Test
        fun `GIVEN Either is Left AND rightOrNull is called THEN null is returned`() {
            assertThat(left.rightOrNull()).isNull()
        }
    }

    @Nested
    inner class RightTests {

        @Test
        fun `GIVEN Either is Right AND either is called THEN fnR is invoked`() {
            // WHEN
            right.either(mockFnL, mockFnR)

            // THEN
            verify { mockFnR.invoke(mockValue) }
            verify { mockFnL wasNot Called }
        }

        @Test
        fun `GIVEN Either is Right AND leftOrNull is called THEN null is returned`() {
            assertThat(right.leftOrNull()).isNull()
        }

        @Test
        fun `GIVEN Either is Right AND rightOrNull is called THEN right value is returned`() {
            assertThat(right.rightOrNull()).isEqualTo(mockValue)
        }
    }
}
