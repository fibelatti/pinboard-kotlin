package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.fibelatti.pinboard.R

class ProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.progressBarStyle
) : ProgressBar(context, attrs, defStyleAttr) {

    init {
        indeterminateDrawable?.setColorFilter(
            ContextCompat.getColor(context, R.color.color_primary),
            PorterDuff.Mode.SRC_IN
        )
    }
}
