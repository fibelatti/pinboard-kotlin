package com.fibelatti.pinboard.core.android.composable

import android.text.TextUtils
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpannable
import com.fibelatti.core.android.extension.setupLinks
import com.fibelatti.pinboard.core.android.CustomQuoteSpan
import com.google.android.material.textview.MaterialTextView

@Composable
fun TextWithBlockquote(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    maxLines: Int = Int.MAX_VALUE,
    blockquoteBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    blockquoteStripeColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    clickableLinks: Boolean = true,
) {
    val argTextColor = textColor.toArgb()
    val rgbBlockquoteBackgroundColor = blockquoteBackgroundColor.toArgb()
    val rgbBlockquoteStripeColor = blockquoteStripeColor.toArgb()
    val stripeWidth = with(LocalDensity.current) { 2.dp.toPx() }
    val gap = with(LocalDensity.current) { 8.dp.toPx() }

    val trimTrailingWhitespace = { source: CharSequence ->
        var i = source.length
        do {
            --i
        } while (i >= 0 && Character.isWhitespace(source[i]))
        source.subSequence(0, i + 1)
    }

    val formattedText = remember(text) {
        HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toSpannable().apply {
            CustomQuoteSpan.replaceQuoteSpans(
                spannable = this,
                backgroundColor = rgbBlockquoteBackgroundColor,
                stripeColor = rgbBlockquoteStripeColor,
                stripeWidth = stripeWidth,
                gap = gap,
            )
        }.let(trimTrailingWhitespace)
    }

    AndroidView(
        factory = ::MaterialTextView,
        modifier = modifier,
        update = { materialTextView ->
            materialTextView.apply {
                this.text = formattedText
                this.textSize = textSize.value
                this.maxLines = maxLines
                this.ellipsize = TextUtils.TruncateAt.END
                this.setTextColor(argTextColor)

                if (clickableLinks) setupLinks()
            }
        },
    )
}
