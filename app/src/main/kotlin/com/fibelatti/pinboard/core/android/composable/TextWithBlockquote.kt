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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpannable
import com.fibelatti.core.extension.setupLinks
import com.fibelatti.pinboard.core.android.CustomQuoteSpan
import com.google.android.material.textview.MaterialTextView

@Composable
fun TextWithBlockquote(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textSize: TextUnit = 14.sp,
    maxLines: Int = Int.MAX_VALUE,
    blockquoteBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    blockquoteStripeColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val argTextColor = textColor.toArgb()
    val rgbBlockquoteBackgroundColor = blockquoteBackgroundColor.toArgb()
    val rgbBlockquoteStripeColor = blockquoteStripeColor.toArgb()
    val stripeWidth = with(LocalDensity.current) { 2.dp.toPx() }
    val gap = with(LocalDensity.current) { 8.dp.toPx() }

    val formattedText = remember(text) {
        HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY).toSpannable().apply {
            CustomQuoteSpan.replaceQuoteSpans(
                spannable = this,
                backgroundColor = rgbBlockquoteBackgroundColor,
                stripeColor = rgbBlockquoteStripeColor,
                stripeWidth = stripeWidth,
                gap = gap,
            )
        }
    }

    AndroidView(
        factory = { context ->
            MaterialTextView(context).apply {
                this.text = formattedText
                this.textSize = textSize.value
                this.maxLines = maxLines
                this.ellipsize = TextUtils.TruncateAt.END
                this.setTextColor(argTextColor)
                setupLinks()
            }
        },
        modifier = modifier,
    )
}
