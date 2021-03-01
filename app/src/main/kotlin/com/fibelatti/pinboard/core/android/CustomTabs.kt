package com.fibelatti.pinboard.core.android

import android.graphics.Rect
import android.net.Uri
import android.text.Spannable
import android.text.Spanned
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.fibelatti.pinboard.R

class CustomTabsURLSpan(url: String) : URLSpan(url) {

    override fun onClick(widget: View) {
        val color = ContextCompat.getColor(widget.context, R.color.color_background)
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setNavigationBarColor(color)
                    .setNavigationBarDividerColor(color)
                    .setToolbarColor(color)
                    .setSecondaryToolbarColor(color)
                    .build()
                )
            .build()
            .launchUrl(widget.context, Uri.parse(url))
    }
}

class LinkTransformationMethod : TransformationMethod {

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        if (view is TextView && view.text is Spannable) {
            val text = view.text as Spannable
            val spans = text.getSpans(0, view.length(), URLSpan::class.java)

            for (span in spans) {
                val spanStart = text.getSpanStart(span)
                val spanEnd = text.getSpanEnd(span)
                val url = span.url

                text.removeSpan(span)
                text.setSpan(
                    CustomTabsURLSpan(url),
                    spanStart,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            return text
        }

        return source
    }

    override fun onFocusChanged(
        view: View?,
        sourceText: CharSequence?,
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        // Intentionally empty
    }
}
