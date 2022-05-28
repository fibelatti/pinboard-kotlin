package com.fibelatti.pinboard.features.posts.presentation

import androidx.recyclerview.widget.DiffUtil
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class PostListDiffResultFactory @Inject constructor() {

    fun create(
        oldList: List<Post>,
        newList: List<Post>,
    ): DiffUtil.DiffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].hash == newList[newItemPosition].hash
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    })
}
