package com.fibelatti.pinboard.features.posts.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fibelatti.core.extension.inflate
import com.fibelatti.pinboard.R
import kotlinx.android.synthetic.main.list_item_tag_simple.view.*
import javax.inject.Inject

class SuggestedTagsAdapter @Inject constructor() :
    ListAdapter<String, SuggestedTagsAdapter.ViewHolder>(DIFF_UTIL) {

    var onTagClicked: ((tag: String) -> Unit)? = null

    override fun getItemCount(): Int = currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun clearItems() {
        submitList(emptyList())
    }

    private fun View.bindView(item: String) {
        textViewTagName.text = item
        setOnClickListener { onTagClicked?.invoke(item) }
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.list_item_tag_simple)
    ) {
        fun bind(item: String) = itemView.bindView(item)
    }

    companion object {

        @JvmStatic
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }
}
