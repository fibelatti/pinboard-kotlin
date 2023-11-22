package com.fibelatti.ui.foundation

import androidx.compose.runtime.Immutable

/**
 * An [Immutable] class that wraps a [List] of [T] in order to provide stability to a composable
 * when needed. The given `value` is assumed to be immutable.
 */
@Immutable
class StableList<T> internal constructor(value: List<T>) : List<T> by value

fun <T> stableListOf(vararg items: T): StableList<T> = StableList(value = items.toList())

fun <T> List<T>.toStableList(): StableList<T> = StableList(value = toList())

fun <T> Array<T>.toStableList(): StableList<T> = StableList(value = toList())
