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
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.appstate.SortType
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

    fun hideTitle() {
        textViewTitle.gone()
        textViewCount.gone()
    }

    fun setTitle(@StringRes titleRes: Int) {
        setTitle(context.getString(titleRes))
    }

    fun setTitle(title: String) {
        if (title.isNotEmpty()) textViewTitle.visible(title) else hideTitle()
        textViewCount.gone()
    }

    fun setPostListTitle(title: String, count: Int, sortType: SortType) {
        textViewTitle.visible(title)
        setPostCount(count, sortType)
    }

    private fun setPostCount(count: Int, sortType: SortType) {
        val countFormatArg = if (count % API_PAGE_SIZE == 0) "$count+" else count.toString()
        val countString = resources.getQuantityString(R.plurals.posts_quantity, count, countFormatArg)
        val countWithSort = resources.getString(
            if (sortType == NewestFirst) {
                R.string.posts_sorting_newest_first
            } else {
                R.string.posts_sorting_oldest_first
            },
            countString
        )

        textViewCount.visible(countWithSort)
    }

    fun setNoteListTitle(title: String, count: Int) {
        textViewTitle.visible(title)
        textViewCount.visible(resources.getQuantityString(R.plurals.notes_quantity, count, count))
    }

    fun hidePostCount() {
        textViewCount.gone()
    }
}
