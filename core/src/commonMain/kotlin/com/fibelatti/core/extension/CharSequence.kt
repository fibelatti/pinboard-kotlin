package com.fibelatti.core.extension

/**
 * Similar to [ifBlank] but with a null check on top.
 */
public inline fun <C, R> C?.ifNullOrBlank(defaultValue: () -> R): R where R : CharSequence, C : R =
    if (isNullOrBlank()) defaultValue() else this
