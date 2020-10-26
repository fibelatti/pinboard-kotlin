package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.databinding.LayoutTitleBinding

class TitleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutTitleBinding.inflate(LayoutInflater.from(context), this, true)

    fun setNavigateUp(@DrawableRes iconRes: Int = R.drawable.ic_back_arrow, navigateUp: () -> Unit) {
        binding.buttonNavigateBack.apply {
            setImageDrawable(ContextCompat.getDrawable(context, iconRes))
            setOnClickListener { navigateUp() }
            visible()
        }
    }

    fun hideNavigateUp() {
        binding.buttonNavigateBack.setOnClickListener(null)
        binding.buttonNavigateBack.gone()
    }

    fun setTitle(@StringRes titleRes: Int) {
        setTitle(context.getString(titleRes))
    }

    fun setTitle(title: String) {
        if (title.isNotEmpty()) binding.textViewTitle.visible(title) else hideTitle()
    }

    fun hideTitle() {
        binding.textViewTitle.gone()
    }

    fun setSubTitle(title: String) {
        if (title.isNotEmpty()) binding.textViewSubtitle.visible(title) else hideSubTitle()
    }

    fun hideSubTitle() {
        binding.textViewSubtitle.gone()
    }

    fun setActionButton(@StringRes stringRes: Int, onClick: () -> Unit) {
        binding.buttonAction.setOnClickListener { onClick() }
        binding.buttonAction.visible(stringRes)
    }

    fun hideActionButton() {
        binding.buttonAction.setOnClickListener(null)
        binding.buttonAction.gone()
    }
}
