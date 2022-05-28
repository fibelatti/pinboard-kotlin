@file:Suppress("TooGenericExceptionCaught")

package com.fibelatti.core.functional

import com.fibelatti.core.functional.Either.Left
import com.fibelatti.core.functional.Either.Right

/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Left] or [Right].
 * FP Convention dictates that [Left] is used for "failure" and [Right] is used for "success".
 *
 * @see Left
 * @see Right
 */
sealed class Either<out L, out R> {

    val isRight: Boolean get() = this is Right<R>
    val isLeft: Boolean get() = this is Left<L>

    /**
     * Calls a function depending on the type of `this`, with its value as a parameter.
     *
     * @param fnL is invoked if `this` is [Left]
     * @param fnR is invoked if `this` is [Right]
     *
     * @return the result of the invoked function
     */
    inline fun either(fnL: (L) -> Unit, fnR: (R) -> Unit) {
        when (this) {
            is Left -> fnL(value)
            is Right -> fnR(value)
        }
    }

    /**
     * @return `this` value if it is [Left], null otherwise
     */
    fun leftOrNull(): L? = when (this) {
        is Left -> value
        is Right -> null
    }

    /**
     * @return `this` value if it is [Right], null otherwise
     */
    fun rightOrNull(): R? = when (this) {
        is Left -> null
        is Right -> value
    }

    /**
     * Represents the left side of [Either] class which by convention is a "Failure".
     */
    data class Left<out L>(val value: L) : Either<L, Nothing>()

    /**
     * Represents the right side of [Either] class which by convention is a "Success".
     */
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

// region Result
/**
 * Alias to [Either] to be used in scenarios which the Failure is a [Throwable].
 */
typealias Result<T> = Either<Throwable, T>

/**
 * Semantic alias to [Right] that indicates a successful result.
 */
typealias Success<T> = Right<T>

/**
 * Semantic alias to [Left] that indicates a failed result.
 */
typealias Failure = Left<Throwable>

/**
 * Semantic alias to value of [Left].
 */
val Failure.error: Throwable get() = value

/**
 * Semantic alias to check if `this` is an instance of [Success].
 */
val <R> Result<R>.isSuccess: Boolean get() = isRight

/**
 * Semantic alias to check if `this` is an instance of [Failure].
 */
val <R> Result<R>.isFailure: Boolean get() = isLeft

/**
 * @return this value if [Success], null otherwise
 */
fun <R> Result<R>.getOrNull(): R? = rightOrNull()

/**
 * @return this error if [Failure], null otherwise
 */
fun <R> Result<R>.exceptionOrNull(): Throwable? = leftOrNull()

/**
 * Throws the exception held by [Left], if `this` is an instance of [Left].
 */
fun <R> Result<R>.throwOnFailure() {
    if (this is Failure) throw this.error
}

/**
 * @return the value if `this` is an instance of [Success]
 * @throws Throwable if `this` is an instance of [Failure]
 */
fun <R> Result<R>.getOrThrow(): R = when (this) {
    is Success -> this.value
    is Failure -> throw this.error
}

/**
 * Shorthand to handle error scenarios depending on the exception received.
 *
 * @param onFailure function to be invoked with the exception as a parameter if `this` is an instance of [Failure]
 *
 * @return `this` value if it is an instance of [Success], the result of [onFailure] otherwise
 */
inline fun <R> Result<R>.getOrElse(onFailure: (exception: Throwable) -> R): R = when (this) {
    is Success -> this.value
    is Failure -> onFailure(this.error)
}

/**
 * Shorthand to handle error scenarios with a default value.
 *
 * @param defaultValue value to be returned if `this` is an instance of [Failure]
 *
 * @return `this` value if it is an instance of [Success], the [defaultValue] otherwise
 */
fun <R> Result<R>.getOrDefault(defaultValue: R): R = when (this) {
    is Success -> this.value
    is Failure -> defaultValue
}

/**
 * Shorthand to handle error scenarios with a different value as a [Result].
 *
 * @param value to be returned wrapped in a [Success] if `this` is an instance of [Failure]
 *
 * @return `this` if it is an instance of [Success], a [Success] of value otherwise
 */
fun <R> Result<R>.onFailureReturn(value: R): Result<R> = when (this) {
    is Success -> this
    is Failure -> Success(value)
}

/**
 * Alias to [Either.either], that will call the respective function depending on the instance of `this`.
 *
 * @param onSuccess function to be called if `this` is an instance of [Success]
 * @param onSuccess function to be called if `this` is an instance of [Failure]
 *
 * @return the result of the invoked function
 */
inline fun <R> Result<R>.fold(onSuccess: (R) -> R, onFailure: (Throwable) -> R): R = when (this) {
    is Success -> onSuccess(this.value)
    is Failure -> onFailure(this.error)
}

/**
 * Calls a mapper function with the value of `this` as its parameter if `this` is an instance of [Success].
 *
 * @param onSuccess mapper function to be invoked with `this` value
 *
 * @return the result of [onSuccess] if `this` is an instance of [Success], `this` otherwise
 */
inline fun <T, R> Result<T>.map(onSuccess: (T) -> Result<R>): Result<R> = when (this) {
    is Success -> onSuccess(this.value)
    is Failure -> this
}

/**
 * Calls a mapper function wrapped by [catching] with the value of `this` as its parameter if `this`
 * is an instance of [Success].
 *
 * @param fn mapper function to be invoked with `this` value
 *
 * @return the result of [fn] if `this` is an instance of [Success], `this` otherwise
 */
inline fun <T, R> Result<T>.mapCatching(fn: (T) -> R): Result<R> = when (this) {
    is Success -> catching { fn(this.value) }
    is Failure -> this
}

/**
 * Wraps [block] in a try catch.
 *
 * @return a [Success] with the result of [block] as its value if successful, [Failure] with the exception as its value
 * if an exception was thrown
 */
inline fun <R> catching(block: () -> R): Result<R> = try {
    Success(block())
} catch (exception: Throwable) {
    Failure(exception)
}

/**
 * Calls [action] using [also] if `this` is an instance of [Failure].
 *
 * @param action code block to be invoked if `this` is an instance of [Failure], with its error as a parameter
 *
 * @return `this`
 */
inline fun <T> Result<T>.onFailure(action: (exception: Throwable) -> Unit): Result<T> = also {
    it.exceptionOrNull()?.let(action)
}

/**
 * Calls [action] using [also] if `this` is an instance of [Success].
 *
 * @param action code block to be invoked if `this` is an instance of [Success], with its value as a parameter
 *
 * @return `this`
 */
inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> = also {
    it.getOrNull()?.let(action)
}
// endregion
