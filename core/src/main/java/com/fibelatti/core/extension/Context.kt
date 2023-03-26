package com.fibelatti.core.extension

import android.content.Context
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
