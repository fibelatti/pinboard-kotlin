package com.fibelatti.core.extension

/**
 * Calls the specified function [block] with [p1] and [p2] values if they are not null as its
 * arguments and returns its result.
 */
inline fun <T1 : Any, T2 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    block: (T1, T2) -> R?,
): R? = if (p1 != null && p2 != null) block(p1, p2) else null

/**
 * Calls the specified function [block] with [p1], [p2] and [p3] values if they are not null as its
 * arguments and returns its result.
 */
inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?,
): R? = if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null

/**
 * Calls the specified function [block] with [p1], [p2], [p3] and [p4] values if they are not null
 * as its arguments and returns its result.
 */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    block: (T1, T2, T3, T4) -> R?,
): R? = if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null

/**
 * Calls the specified function [block] with [p1], [p2], [p3], [p4] and [p5] values if they are not
 * null as its arguments and returns its result.
 */
inline fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    p4: T4?,
    p5: T5?,
    block: (T1, T2, T3, T4, T5) -> R?,
): R? = if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null

inline fun <T, reified CastedT> T.applyAs(block: CastedT.() -> Unit): T {
    if (this is CastedT) this.block()
    return this
}
