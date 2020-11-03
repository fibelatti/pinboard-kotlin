package com.fibelatti.pinboard.core.extension

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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

fun View.smoothScrollY(scrollBy: Int) {
    ObjectAnimator.ofInt(this, "scrollY", scrollBy)
        .apply { interpolator = AccelerateDecelerateInterpolator() }
        .setDuration(resources.getInteger(R.integer.anim_time_long).toLong())
        .start()
}
