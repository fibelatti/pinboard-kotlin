package com.fibelatti.pinboard.core.extension

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import com.fibelatti.pinboard.core.android.LinkTransformationMethod

fun TextView.setupLinks() {
    Linkify.addLinks(this, Linkify.ALL)
    movementMethod = LinkMovementMethod.getInstance()
    transformationMethod = LinkTransformationMethod()
}
