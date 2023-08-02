package com.fibelatti.pinboard.features.appstate

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A base class for ActionHandlers that can take [Action]s of type [A] and return [Content].
 */
abstract class ActionHandler<A : Action> {

    /**
     * Base method to apply [Action] of type [A] to the [currentContent], returning an updated [Content].
     *
     * @param action an [Action] of type [A]
     * @param currentContent the [Content] to be updated
     *
     * @return an updated [Content]
     */
    abstract suspend fun runAction(action: A, currentContent: Content): Content

    /**
     * Service method to check if [this] is of type [T] before running [body] with it as a parameter.
     * If the type doesn't match then the receiver is returned unchanged.
     *
     * @receiver the [Content] to be checked
     * @param T the expected [Content] type
     * @param body the function to invoke if the type matches
     * @return the result of [body] if [this] is of type [T], [this] unchanged otherwise.
     */
    @OptIn(ExperimentalContracts::class)
    inline fun <reified T : Content> Content.reduce(
        body: (T) -> Content,
    ): Content {
        contract {
            callsInPlace(body, InvocationKind.AT_MOST_ONCE)
        }
        return if (this is T) body(this) else this
    }
}
