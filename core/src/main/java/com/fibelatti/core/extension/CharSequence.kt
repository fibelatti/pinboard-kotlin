package com.fibelatti.core.extension

/**
 * Similar to [ifBlank] but with a null check on top.
 */
inline fun <C, R> C?.ifNullOrBlank(defaultValue: () -> R): R where R : CharSequence, C : R =
    if (this == null || isBlank()) defaultValue() else this
