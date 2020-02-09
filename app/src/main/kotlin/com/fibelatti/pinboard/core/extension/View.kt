package com.fibelatti.pinboard.core.extension

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.NestedScrollingParent
import androidx.recyclerview.widget.RecyclerView
import com.fibelatti.core.extension.children
import com.fibelatti.pinboard.R
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun BottomAppBar.show() {
    animate().translationY(0f)
        .setDuration(resources.getInteger(R.integer.anim_time_default).toLong())
        .start()
}

fun FloatingActionButton.blink(onHidden: () -> Unit = {}) {
    hide(object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)
            onHidden()
            show()
        }
    })
}

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
fun getViewToApplyInsets(rootView: View): View {
    return when (rootView) {
        is ScrollView,
        is NestedScrollingParent -> (rootView as? ViewGroup)?.children?.lastOrNull()
        is RecyclerView -> rootView
        is ViewGroup -> {
            rootView.children
                .lastOrNull {
                    it is ScrollView || it is NestedScrollingParent || it is RecyclerView
                }?.let(::getViewToApplyInsets)
                ?: rootView.children.lastOrNull()?.let(::getViewToApplyInsets)
        }
        else -> rootView
    } ?: rootView
}
