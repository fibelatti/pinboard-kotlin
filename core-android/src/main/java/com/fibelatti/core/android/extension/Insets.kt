@file:Suppress("Unused")

package com.fibelatti.core.android.extension

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Helper function to manage callbacks of [View.setOnApplyWindowInsetsListener].
 *
 * @param f the function to be invoked when [View.setOnApplyWindowInsetsListener] is called.
 */
public fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, InitialPadding, InitialMargin) -> Unit) {
    val initialPadding = InitialPadding(
        start = paddingStart,
        top = paddingTop,
        end = paddingEnd,
        bottom = paddingBottom,
    )
    val initialMargin = (layoutParams as? ViewGroup.MarginLayoutParams)
        ?.run { InitialMargin(leftMargin, topMargin, rightMargin, bottomMargin) }
        ?: InitialMargin(0, 0, 0, 0)

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding, initialMargin)
        insets
    }

    requestApplyInsetsWhenAttached()
}

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal.
        ViewCompat.requestApplyInsets(this)
    } else {
        // We're not attached to the hierarchy, add a listener to request when we are.
        addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(view)
                }

                override fun onViewDetachedFromWindow(v: View) {
                    // Intentionally empty
                }
            },
        )
    }
}

/**
 * The initial padding values of a [View].
 *
 * @see [View.doOnApplyWindowInsets]
 */
public data class InitialPadding(val start: Int, val top: Int, val end: Int, val bottom: Int)

/**
 * The initial margin values of a [View].
 *
 * @see [View.doOnApplyWindowInsets]
 */
public data class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)
