package com.fibelatti.pinboard.core.android.customview

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inflate
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import kotlinx.android.synthetic.main.layout_title.view.*

class TitleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.layout_title, true)
    }

    fun setNavigateUp(@DrawableRes iconRes: Int = R.drawable.ic_back_arrow, navigateUp: () -> Unit) {
        buttonNavigateBack.apply {
            setImageDrawable(ContextCompat.getDrawable(context, iconRes))
            setOnClickListener { navigateUp() }
            visible()
        }
    }

    fun hideNavigateUp() {
        buttonNavigateBack.setOnClickListener(null)
        buttonNavigateBack.gone()
    }

    fun setTitle(@StringRes titleRes: Int) {
        setTitle(context.getString(titleRes))
    }

    fun setTitle(title: String) {
        if (title.isNotEmpty()) textViewTitle.visible(title) else hideTitle()
    }

    fun hideTitle() {
        textViewTitle.gone()
    }

    fun setSubTitle(title: String) {
        if (title.isNotEmpty()) textViewSubtitle.visible(title) else hideSubTitle()
    }

    fun hideSubTitle() {
        textViewSubtitle.gone()
    }

    fun setActionButton(@StringRes stringRes: Int, onClick: () -> Unit) {
        buttonAction.setOnClickListener { onClick() }
        buttonAction.visible(stringRes)
    }

    fun hideActionButton() {
        buttonAction.setOnClickListener(null)
        buttonAction.gone()
    }
}
