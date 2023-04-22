package com.fibelatti.core.functional

import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail

class EitherTest {

    private val mockValue = true
    private val mockDefaultValue = false
    private val mockError = mockk<Exception>()

    private val right: Either<Throwable, Boolean> = Either.Right(mockValue)
    private val left: Either<Throwable, Boolean> = Either.Left(mockError)

    private val mockFnR = mockk<(Boolean) -> Unit>(relaxed = true)
    private val mockFnResultR = mockk<(Boolean) -> Result<Unit>> {
        every { this@mockk.invoke(any()) } returns Success(Unit)
    }
    private val mockFnL = mockk<(Throwable) -> Unit>(relaxed = true)

    private val success = Success(mockValue)
    private val failure = Failure(mockError)

    private val mockOnSuccess = mockk<(Boolean) -> Boolean> {
        every { this@mockk.invoke(any()) } answers { args.first() as Boolean }
    }
    private val mockOnFailure = mockk<(Throwable) -> Boolean> {
        every { this@mockk.invoke(any()) } returns mockDefaultValue
    }

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

    @Nested
    inner class ResultSuccessTests {

        @Test
        fun `GIVEN Result is Success WHEN value is called THEN value is returned`() {
            assertThat(success.value).isEqualTo(mockValue)
        }

        @Test
        fun `GIVEN Result is Success WHEN isSuccess is called THEN true is returned`() {
            assertThat(success.isSuccess).isTrue()
        }

        @Test
        fun `GIVEN Result is Success WHEN isFailure is called THEN false is returned`() {
            assertThat(success.isFailure).isFalse()
        }

        @Test
        fun `GIVEN Result is Success WHEN getOrNull is called THEN value is returned`() {
            assertThat(success.getOrNull()).isEqualTo(mockValue)
        }

        @Test
        fun `GIVEN Result is Success WHEN exceptionOrNull is called THEN null is returned`() {
            assertThat(success.exceptionOrNull()).isNull()
        }

        @Test
        fun `GIVEN Result is Success WHEN throwOnFailure is called THEN nothing happens`() {
            try {
                success.throwOnFailure()
            } catch (ignored: Exception) {
                fail { "Unexpected exception thrown" }
            }
        }

        @Test
        fun `GIVEN Result is Success WHEN getOrThrow is called THEN value is returned`() {
            try {
                assertThat(success.getOrThrow()).isEqualTo(mockValue)
            } catch (ignored: Exception) {
                fail { "Unexpected exception thrown" }
            }
        }

        @Test
        fun `GIVEN Result is Success WHEN getOrElse is called THEN value is returned`() {
            assertThat(success.getOrElse(mockFnL)).isEqualTo(mockValue)
            verify { mockFnL wasNot Called }
        }

        @Test
        fun `GIVEN Result is Success WHEN getOrDefault is called THEN value is returned`() {
            assertThat(success.getOrDefault(mockDefaultValue)).isEqualTo(mockValue)
        }

        @Test
        fun `GIVEN Result is Success WHEN onFailureReturn is called THEN Result is returned`() {
            assertThat(success.onFailureReturn(false)).isEqualTo(success)
        }

        @Test
        fun `GIVEN Result is Failure WHEN onFailureReturn is called THEN Success of the value is returned`() {
            assertThat(failure.onFailureReturn(false)).isEqualTo(Success(false))
        }

        @Test
        fun `GIVEN Result is Success WHEN fold is called THEN onSuccess is called`() {
            // WHEN
            success.fold(mockOnSuccess, mockOnFailure)

            // THEN
            verify { mockOnSuccess(mockValue) }
            verify { mockOnFailure wasNot Called }
        }

        @Test
        fun `GIVEN Result is Success WHEN onFailure is called THEN nothing happens`() {
            // WHEN
            success.onFailure(mockFnL)

            // THEN
            verify { mockFnL wasNot Called }
        }

        @Test
        fun `GIVEN Result is Success WHEN onSuccess is called THEN function is invoked`() {
            // WHEN
            success.onSuccess(mockFnR)

            // THEN
            verify { mockFnR(mockValue) }
        }

        @Test
        fun `GIVEN Result is Success WHEN map is called THEN function is invoked`() {
            // WHEN
            success.map(mockFnResultR)

            // THEN
            verify { mockFnResultR(mockValue) }
        }

        @Test
        fun `GIVEN Result is Success WHEN mapCatching is called THEN function is invoked`() {
            // WHEN
            success.mapCatching(mockFnR)

            // THEN
            verify { mockFnR(mockValue) }
        }
    }

    @Nested
    inner class ResultFailureTests {

        @Test
        fun `GIVEN Result is Failure WHEN error is called THEN value is returned`() {
            assertThat(failure.error).isEqualTo(mockError)
        }

        @Test
        fun `GIVEN Result is Failure WHEN isSuccess is called THEN false is returned`() {
            assertThat(failure.isSuccess).isFalse()
        }

        @Test
        fun `GIVEN Result is Failure WHEN isFailure is called THEN true is returned`() {
            assertThat(failure.isFailure).isTrue()
        }

        @Test
        fun `GIVEN Result is Failure WHEN getOrNull is called THEN null is returned`() {
            assertThat(failure.getOrNull<Boolean>()).isNull()
        }

        @Test
        fun `GIVEN Result is Failure WHEN exceptionOrNull is called THEN error is returned`() {
            assertThat(failure.exceptionOrNull()).isEqualTo(mockError)
        }

        @Test
        fun `GIVEN Result is Failure WHEN throwOnFailure is called THEN error is thrown`() {
            assertThrows<Exception> { failure.throwOnFailure() }
        }

        @Test
        fun `GIVEN Result is Failure WHEN getOrThrow is called THEN error is thrown`() {
            assertThrows<Exception> { failure.getOrThrow() }
        }

        @Test
        fun `GIVEN Result is Failure WHEN getOrElse is called THEN function is invoked and its result returned`() {
            assertThat(failure.getOrElse(mockOnFailure)).isEqualTo(mockDefaultValue)
        }

        @Test
        fun `GIVEN Result is Failure WHEN getOrDefault is called THEN default value is returned`() {
            assertThat(failure.getOrDefault(mockDefaultValue)).isEqualTo(mockDefaultValue)
        }

        @Test
        fun `GIVEN Result is Failure WHEN fold is called THEN onFailure is called`() {
            // WHEN
            failure.fold(mockOnSuccess, mockOnFailure)

            // THEN
            verify { mockOnSuccess wasNot Called }
            verify { mockOnFailure(mockError) }
        }

        @Test
        fun `GIVEN Result is Failure WHEN onFailure is called THEN function is invoked`() {
            // WHEN
            failure.onFailure(mockFnL)

            // THEN
            verify { mockFnL(mockError) }
        }

        @Test
        fun `GIVEN Result is Failure WHEN onSuccess is called THEN nothing happens`() {
            // WHEN
            failure.onSuccess(mockFnR)

            // THEN
            verify { mockFnR wasNot Called }
        }

        @Test
        fun `GIVEN Result is Failure WHEN map is called THEN error is returned`() {
            // WHEN
            val result = failure.map(mockFnResultR)

            // THEN
            assertThat(result).isEqualTo(failure)
            verify { mockFnResultR wasNot Called }
        }

        @Test
        fun `GIVEN Result is Failure WHEN mapCatching is called THEN error is returned`() {
            assertThat(failure.mapCatching(mockOnSuccess)).isEqualTo(failure)
        }
    }

    @Nested
    inner class ResultTests {

        @Test
        fun `GIVEN catching is called WHEN block throws an exception THEN Failure is returned`() {
            // GIVEN
            val function = spyk<() -> Boolean> {
                every { this@spyk.invoke() } answers { throw mockError }
            }

            // THEN
            assertThat(catching(function)).isEqualTo(Failure(mockError))
        }

        @Test
        fun `WHEN catching is called THEN Success is returned`() {
            // GIVEN
            val function = { true }

            // THEN
            assertThat(catching(function)).isEqualTo(Success(true))
        }
    }
}
