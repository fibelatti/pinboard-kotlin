package com.fibelatti.bookmarking

import kotlin.reflect.KClass

/**
 * Returns a list of all Sealed Subclasses of the receiver, including their own Sealed Subclasses
 */
val <T : Any> KClass<T>.allSealedSubclasses: List<KClass<out T>>
    get() = sealedSubclasses.flatten()

/**
 * Given a list of Sealed Subclasses will recursively create a flattened list of containing them and
 * their children, and their children, and their children...
 */
fun <T : Any> List<KClass<out T>>.flatten(): List<KClass<out T>> {
    return flatMap {
        if (it.sealedSubclasses.isNotEmpty()) {
            it.sealedSubclasses.flatten()
        } else {
            listOf(it)
        }
    }
}
