package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.fibelatti.pinboard.R
import kotlinx.android.synthetic.main.layout_empty_list.view.*

class EmptyListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_empty_list, this, true)
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
