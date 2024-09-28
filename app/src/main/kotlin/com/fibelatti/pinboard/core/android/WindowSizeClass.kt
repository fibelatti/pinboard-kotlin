package com.fibelatti.pinboard.core.android

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.window.layout.WindowMetricsCalculator
import com.fibelatti.core.android.extension.findOwner

enum class WindowSizeClass {

    COMPACT,
    MEDIUM,
    EXPANDED,
    ;

    companion object {

        const val MEDIUM_MIN_WIDTH = 600f
        const val EXPANDED_MIN_WIDTH = 840f
    }
}

fun Activity.computeWidthWindowSizeClass(): WindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    val widthDp = metrics.bounds.width() / resources.displayMetrics.density

    return when {
        widthDp < WindowSizeClass.MEDIUM_MIN_WIDTH -> WindowSizeClass.COMPACT
        widthDp < WindowSizeClass.EXPANDED_MIN_WIDTH -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

fun Context.widthWindowSizeClassReactiveView(
    body: (WindowSizeClass) -> Unit,
): View = object : View(this) {

    init {
        computeWidthWindowSizeClass()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        computeWidthWindowSizeClass()
    }

    private fun computeWidthWindowSizeClass() {
        val activity: Activity = findOwner() ?: return
        val windowSizeClass = activity.computeWidthWindowSizeClass()

        body(windowSizeClass)
    }
}
