package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.fibelatti.core.extension.inflate
import com.fibelatti.pinboard.R
import kotlinx.android.synthetic.main.layout_empty_list.view.*

class EmptyListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.layout_empty_list, true)
    }

    fun setIcon(@DrawableRes drawableRes: Int) {
        imageViewIcon.setImageResource(drawableRes)
    }

    fun setTitle(@StringRes stringRes: Int) {
        textViewEmptyTitle.setText(stringRes)
    }

    fun setDescription(@StringRes stringRes: Int) {
        textViewEmptyDescription.setText(stringRes)
    }
}
