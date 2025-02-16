package com.fibelatti.core.android.extension

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat

/**
 * Returns the first [ContextWrapper] with type [T] found in the hierarchy if any, or null if none
 * could be found.
 */
public inline fun <reified T> Context.findOwner(): T? {
    var innerContext = this
    while (innerContext is ContextWrapper) {
        if (innerContext is T) {
            return innerContext
        }
        innerContext = innerContext.baseContext
    }
    return null
}

/**
 * Return the corresponding [color][ColorInt] for the given [attribute id][attrId].
 */
@ColorInt
public fun Context.getAttributeColor(
    @AttrRes attrId: Int,
    @ColorInt default: Int = -1,
): Int {
    val resolved = obtainStyledAttributes(intArrayOf(attrId))
    val color = resolved.getColor(0, default)
    resolved.recycle()
    return color
}

/**
 * Creates an intent using [ShareCompat] to share the given [text] to other apps.
 *
 * @param title the [StringRes] of the title to be displayed in the chooser
 * @param text the text to be shared
 */
public fun Context.shareText(@StringRes title: Int, text: String) {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle(title)
        .setText(text)
        .startChooser()
}

/**
 * Creates an intent using [ShareCompat] to share the given [text] to other apps.
 *
 * @param title the text of the title to be displayed in the chooser
 * @param text the text to be shared
 */
public fun Context.shareText(title: String, text: String) {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle(title)
        .setText(text)
        .startChooser()
}
