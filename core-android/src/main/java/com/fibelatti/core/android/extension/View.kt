package com.fibelatti.core.android.extension

import android.text.method.LinkMovementMethod
import android.text.method.TransformationMethod
import android.text.util.Linkify
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.getSystemService

/**
 * Shorthand function to linkify all urls in `this`, setting the movement method to [LinkMovementMethod] and the
 * transformation method to [transformationMethod].
 *
 * @param [transformationMethod] an optional [TransformationMethod] to be set as `this` transformationMethod
 */
public fun TextView.setupLinks(transformationMethod: TransformationMethod? = null) {
    Linkify.addLinks(this, Linkify.WEB_URLS)
    movementMethod = LinkMovementMethod.getInstance()
    transformationMethod?.let(::setTransformationMethod)
}

public fun View.showKeyboard() {
    context.getSystemService<InputMethodManager>()?.showSoftInput(this, 0)
}

public fun View.hideKeyboard() {
    context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
}
