package com.fibelatti.pinboard.core.extension

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fibelatti.core.extension.children
import com.fibelatti.pinboard.R
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

@JvmOverloads
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

@JvmOverloads
fun View.snackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    additionalConfiguration: Snackbar.() -> Unit = {}
) {
    Snackbar.make(this, message, duration)
        .apply {
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            val margin = context.resources.getDimensionPixelSize(R.dimen.margin_regular)
            view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                setMargins(margin, margin, margin, margin)
            }
            view.background = ContextCompat.getDrawable(context, R.drawable.background_snackbar)

            additionalConfiguration()
        }
        .show()
}

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

fun getViewToApplyInsets(view: View): View? {
    return when (view) {
        is ScrollView,
        is NestedScrollView -> (view as? ViewGroup)?.children?.firstOrNull()
        is SwipeRefreshLayout -> (view as? ViewGroup)?.children?.lastOrNull()
        is RecyclerView -> view
        is ViewGroup -> {
            view.children.firstOrNull {
                it is ScrollView || it is NestedScrollView || it is SwipeRefreshLayout || it is RecyclerView
            }?.let(::getViewToApplyInsets)
        }
        else -> view
    }
}

fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding, InitialMargin) -> Unit) {

    val initialPadding = InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val initialMargin = (layoutParams as? ViewGroup.MarginLayoutParams)
        ?.run { InitialMargin(leftMargin, topMargin, rightMargin, bottomMargin) }
        ?: InitialMargin(0, 0, 0, 0)

    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding, initialMargin)
        insets
    }
}

data class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

data class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)
