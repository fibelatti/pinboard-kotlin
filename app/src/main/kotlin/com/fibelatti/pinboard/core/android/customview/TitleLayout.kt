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
import kotlinx.android.synthetic.main.layout_title.view.*

class TitleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_title, this, true)
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
        textViewPostCount.gone()
    }

    fun hideTitle() {
        textViewTitle.gone()
        textViewPostCount.gone()
    }

    fun setPostCount(count: Int) {
        textViewPostCount.visible(resources.getQuantityString(R.plurals.posts_quantity, count, count))
    }

    fun hidePostCount() {
        textViewPostCount.gone()
    }
}
