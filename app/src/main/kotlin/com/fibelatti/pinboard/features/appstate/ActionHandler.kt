package com.fibelatti.pinboard.features.appstate

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
    abstract fun runAction(action: A, currentContent: Content): Content

    /**
     * Service method to check if [currentContent] is of type [T] before running [body] with it as a parameter. If type
     * is not matched than the same [currentContent] is returned unchanged.
     *
     * @param T the [Content] type to check
     * @param currentContent the [Content] to be checked
     * @param body the function to invoke if the type matches
     *
     * @return the result of [body] if [currentContent] is of type [T], [currentContent] otherwise
     */
    inline fun <reified T : Content> runOnlyForCurrentContentOfType(
        currentContent: Content,
        body: (T) -> Content
    ): Content {
        return if (currentContent is T) {
            body(currentContent)
        } else {
            currentContent
        }
    }
}
