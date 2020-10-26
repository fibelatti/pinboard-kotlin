package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.fibelatti.pinboard.databinding.LayoutEmptyListBinding

class EmptyListLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutEmptyListBinding.inflate(LayoutInflater.from(context), this, true)

    fun setIcon(@DrawableRes drawableRes: Int) {
        binding.imageViewIcon.setImageResource(drawableRes)
    }

    fun setTitle(@StringRes stringRes: Int) {
        binding.textViewEmptyTitle.setText(stringRes)
    }

    fun setDescription(@StringRes stringRes: Int) {
        binding.textViewEmptyDescription.setText(stringRes)
    }
}
