package com.fibelatti.core.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

/**
 * Return the corresponding [color][ColorInt] for the given [attribute id][attrId].
 */
@ColorInt
fun Context.getAttributeColor(
    @AttrRes attrId: Int,
    @ColorInt default: Int = -1,
): Int {
    val resolved = obtainStyledAttributes(intArrayOf(attrId))
    val color = resolved.getColor(0, default)
    resolved.recycle()
    return color
}

fun Context?.findActivity(): Activity? {
    var currentContext = this
    while (currentContext != null) {
        if (currentContext is Activity) return currentContext
        if (currentContext !is ContextWrapper) break
        currentContext = currentContext.baseContext
    }
    return null
}
