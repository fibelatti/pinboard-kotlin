package com.fibelatti.core.extension

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.NestedScrollingParent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper function to search for a scrolling view to which window insets should be applied.
 *
 * It looks for a direct child of the provided view of type [ScrollView], [NestedScrollingParent] or
 * [RecyclerView]. If no direct child of the listed types is found the function will keep searching
 * using the last child of type [ViewGroup] as the new starting point.
 *
 * The provided view is returned in case no view was found.
 *
 * @param rootView the root [View] to start the search
 *
 * @return the [View] to which insets must be applied
 */
fun getViewToApplyInsets(rootView: View): View? = when (rootView) {
    is ScrollView, is NestedScrollingParent -> (rootView as? ViewGroup)?.children?.lastOrNull()
    is RecyclerView -> rootView
    is ViewGroup -> rootView.children.lastOrNull()?.let(::getViewToApplyInsets)
    else -> null
}

/**
 * Helper function to manage callbacks of [View.setOnApplyWindowInsetsListener].
 *
 * @param f the function to be invoked when [View.setOnApplyWindowInsetsListener] is called.
 */
fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, InitialPadding, InitialMargin) -> Unit) {
    val initialPadding = InitialPadding(
        ViewCompat.getPaddingStart(this),
        paddingTop,
        ViewCompat.getPaddingEnd(this),
        paddingBottom,
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
    if (ViewCompat.isAttachedToWindow(this)) {
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
data class InitialPadding(val start: Int, val top: Int, val end: Int, val bottom: Int)

/**
 * The initial margin values of a [View].
 *
 * @see [View.doOnApplyWindowInsets]
 */
data class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)
