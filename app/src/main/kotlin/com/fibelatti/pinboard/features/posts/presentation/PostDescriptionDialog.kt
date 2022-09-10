package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import com.fibelatti.core.extension.setupLinks
import com.fibelatti.pinboard.core.android.LinkTransformationMethod
import com.fibelatti.pinboard.databinding.FragmentPostDescriptionBinding
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.android.material.bottomsheet.BottomSheetDialog

object PostDescriptionDialog {

    fun showPostDescriptionDialog(
        context: Context,
        post: Post,
    ) {
        BottomSheetDialog(context).apply {
            val dialogLayout = FragmentPostDescriptionBinding.inflate(layoutInflater)
            setContentView(dialogLayout.root)

            dialogLayout.textViewBookmarkUrl.text = post.url
            dialogLayout.textViewBookmarkTitle.text = post.title
            dialogLayout.textViewBookmarkDescription.text = post.description

            val linksTransformationMethod = LinkTransformationMethod()
            dialogLayout.textViewBookmarkUrl.setupLinks(linksTransformationMethod)
            dialogLayout.textViewBookmarkDescription.setupLinks(linksTransformationMethod)
        }.show()
    }
}
