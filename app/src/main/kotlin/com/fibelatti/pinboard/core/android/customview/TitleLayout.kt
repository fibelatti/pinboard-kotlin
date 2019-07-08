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
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.SortType
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

    fun hideTitle() {
        textViewTitle.gone()
        textViewPostCount.gone()
    }

    fun setTitle(@StringRes titleRes: Int) {
        setTitle(context.getString(titleRes))
    }

    fun setTitle(title: String) {
        if (title.isNotEmpty()) textViewTitle.visible(title) else hideTitle()
        textViewPostCount.gone()
    }

    fun setTitle(title: String, count: Int, sortType: SortType) {
        textViewTitle.visible(title)
        setPostCount(count, sortType)
    }

    fun setPostCount(count: Int, sortType: SortType) {
        val countString = resources.getQuantityString(R.plurals.posts_quantity, count, count)
        val countWithSort = resources.getString(
            if (sortType == NewestFirst) {
                R.string.posts_sorting_newest_first
            } else {
                R.string.posts_sorting_oldest_first
            },
            countString
        )

        textViewPostCount.visible(countWithSort)
    }

    fun hidePostCount() {
        textViewPostCount.gone()
    }
}
