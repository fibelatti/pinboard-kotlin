package com.fibelatti.core.android.extension

import android.animation.LayoutTransition
import android.text.method.LinkMovementMethod
import android.text.method.TransformationMethod
import android.text.util.Linkify
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.core.view.updateLayoutParams
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

public fun View.getContentView(): ViewGroup {
    var parent = parent as View
    while (parent.id != android.R.id.content) {
        parent = parent.parent as View
    }

    return parent as ViewGroup
}

/**
 * Set `this` height to 0.
 */
public fun View.heightCollapse() {
    updateLayoutParams<ViewGroup.LayoutParams> {
        height = 0
    }
}

/**
 * @return true if `this` height is 0, false otherwise
 */
public fun View.heightIsCollapsed(): Boolean = layoutParams.height == 0

/**
 * Set `this` height to [ViewGroup.LayoutParams.WRAP_CONTENT]. It handles [LinearLayout],
 * [RelativeLayout] and [FrameLayout] specifically and tries to default to [ViewGroup] otherwise.
 */
public fun View.heightWrapContent() {
    updateLayoutParams<ViewGroup.LayoutParams> {
        height = when (this) {
            is LinearLayout.LayoutParams -> LinearLayout.LayoutParams.WRAP_CONTENT
            is RelativeLayout.LayoutParams -> RelativeLayout.LayoutParams.WRAP_CONTENT
            is FrameLayout.LayoutParams -> FrameLayout.LayoutParams.WRAP_CONTENT
            else -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
}

/**
 * Updates `this` layoutTransition to allow [LayoutTransition.CHANGING] transition types,
 * which is needed to animate height changes, for instance.
 */
public fun ViewGroup.animateChangingTransitions() {
    layoutTransition = LayoutTransition().apply { enableTransitionType(LayoutTransition.CHANGING) }
}

/**
 * Calls [TextView.setCompoundDrawablesWithIntrinsicBounds] with null as values for all parameters.
 */
public fun TextView.clearDrawables() {
    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
}

/**
 * Shorthand function to set compound drawables in a [TextView].
 *
 * @param drawableLeftRes the [DrawableRes] id to be set as a left drawable
 * @param drawableTopRes the [DrawableRes] id to be set as a top drawable
 * @param drawableRightRes the [DrawableRes] id to be set as a right drawable
 * @param drawableBottomRes the [DrawableRes] id to be set as a bottom drawable
 */
public fun TextView.setDrawables(
    @DrawableRes drawableLeftRes: Int? = null,
    @DrawableRes drawableTopRes: Int? = null,
    @DrawableRes drawableRightRes: Int? = null,
    @DrawableRes drawableBottomRes: Int? = null,
) {
    setCompoundDrawablesWithIntrinsicBounds(
        drawableLeftRes?.let { AppCompatResources.getDrawable(context, it) },
        drawableTopRes?.let { AppCompatResources.getDrawable(context, it) },
        drawableRightRes?.let { AppCompatResources.getDrawable(context, it) },
        drawableBottomRes?.let { AppCompatResources.getDrawable(context, it) },
    )
}

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

/**
 * @return `this` text as string, or an empty string if text was null
 */
public fun EditText.textAsString(): String = this.text?.toString().orEmpty()

/**
 * Sets `this` text to an empty string.
 */
public fun EditText.clearText() {
    setText(null)
}

/**
 * Shorthand function for running the given [block] when the [EditText] receives a [handledAction] or a keyboard
 * submit.
 */
public inline fun EditText.onActionOrKeyboardSubmit(
    vararg handledAction: Int,
    crossinline block: EditText.() -> Unit,
) {
    val handledActions = handledAction.toList()

    setOnEditorActionListener { _, actionId, event ->
        val shouldHandle = actionId in handledActions ||
            event != null &&
            event.action == KeyEvent.ACTION_DOWN &&
            event.keyCode == KeyEvent.KEYCODE_ENTER

        return@setOnEditorActionListener if (shouldHandle) {
            block()
            true
        } else {
            false
        }
    }
}

/**
 * Shorthand function to display an error message in a [TextInputLayout].
 *
 * @param errorMessage the message to be displayed
 */
public fun TextInputLayout.showError(errorMessage: String) {
    error = errorMessage
    if (childCount == 1 && (getChildAt(0) is TextInputEditText || getChildAt(0) is EditText)) {
        getChildAt(0).requestFocus()
    }
}

/**
 * Shorthand function to reset the error state of a [TextInputLayout].
 */
public fun TextInputLayout.clearError() {
    error = null
}

public fun View.showKeyboard() {
    context.getSystemService<InputMethodManager>()?.showSoftInput(this, 0)
}

public fun View.hideKeyboard() {
    context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
}
