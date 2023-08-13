package com.fibelatti.pinboard.core.android

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spannable
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.QuoteSpan
import androidx.annotation.ColorInt
import com.fibelatti.pinboard.core.android.CustomQuoteSpan.Companion.replaceQuoteSpans

/**
 * [android.text.style.QuoteSpan] requires min API 28 in order to customize the stripe color and gap,
 * so this is a substitute which also allows customizing the background color.
 *
 * @see replaceQuoteSpans
 */
class CustomQuoteSpan(
    @ColorInt private val backgroundColor: Int,
    @ColorInt private val stripeColor: Int,
    private val stripeWidth: Float,
    private val gap: Float,
) : LeadingMarginSpan, LineBackgroundSpan {

    override fun getLeadingMargin(first: Boolean): Int = (stripeWidth + gap).toInt()

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout,
    ) {
        val style = p.style
        val paintColor = p.color
        p.style = Paint.Style.FILL
        p.color = stripeColor
        c.drawRect(x.toFloat(), top.toFloat(), x + dir * stripeWidth, bottom.toFloat(), p)
        p.style = style
        p.color = paintColor
    }

    override fun drawBackground(
        c: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int,
    ) {
        val paintColor = p.color
        p.color = backgroundColor
        c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), p)
        p.color = paintColor
    }

    companion object {

        fun replaceQuoteSpans(
            spannable: Spannable,
            @ColorInt backgroundColor: Int,
            @ColorInt stripeColor: Int,
            stripeWidth: Float,
            gap: Float,
        ) {
            val quoteSpans = spannable.getSpans(0, spannable.length, QuoteSpan::class.java)
            for (quoteSpan in quoteSpans) {
                val start = spannable.getSpanStart(quoteSpan)
                val end = spannable.getSpanEnd(quoteSpan)
                val flags = spannable.getSpanFlags(quoteSpan)

                spannable.removeSpan(quoteSpan)
                spannable.setSpan(CustomQuoteSpan(backgroundColor, stripeColor, stripeWidth, gap), start, end, flags)
            }
        }
    }
}
