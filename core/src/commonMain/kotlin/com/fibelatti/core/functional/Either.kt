package com.fibelatti.core.functional

/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Left] or [Right].
 * FP Convention dictates that [Left] is used for "failure" and [Right] is used for "success".
 *
 * @see Left
 * @see Right
 */
public sealed class Either<out L, out R> {

    public val isRight: Boolean get() = this is Right<R>
    public val isLeft: Boolean get() = this is Left<L>

    /**
     * Calls a function depending on the type of `this`, with its value as a parameter.
     *
     * @param fnL is invoked if `this` is [Left]
     * @param fnR is invoked if `this` is [Right]
     *
     * @return the result of the invoked function
     */
    public inline fun either(fnL: (L) -> Unit, fnR: (R) -> Unit) {
        when (this) {
            is Left -> fnL(value)
            is Right -> fnR(value)
        }
    }

    /**
     * @return `this` value if it is [Left], null otherwise
     */
    public fun leftOrNull(): L? = when (this) {
        is Left -> value
        is Right -> null
    }

    /**
     * @return `this` value if it is [Right], null otherwise
     */
    public fun rightOrNull(): R? = when (this) {
        is Left -> null
        is Right -> value
    }

    /**
     * Represents the left side of [Either] class which by convention is a "Failure".
     */
    public data class Left<out L>(val value: L) : Either<L, Nothing>()

    /**
     * Represents the right side of [Either] class which by convention is a "Success".
     */
    public data class Right<out R>(val value: R) : Either<Nothing, R>()
}
