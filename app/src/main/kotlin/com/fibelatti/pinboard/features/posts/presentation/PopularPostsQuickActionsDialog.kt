package com.fibelatti.pinboard.features.posts.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.features.posts.domain.model.Post

object PopularPostsQuickActionsDialog {

    fun show(
        context: Context,
        post: Post,
        onSave: (Post) -> Unit,
    ) {
        SelectionDialog.show(
            context = context,
            title = context.getString(R.string.quick_actions_title),
            options = PopularPostQuickActions.allOptions(post),
            optionName = { option -> context.getString(option.title) },
            optionIcon = PopularPostQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is PopularPostQuickActions.Save -> onSave(option.post)

                    is PopularPostQuickActions.CopyUrl -> context.copyToClipboard(
                        label = post.displayTitle,
                        text = post.url,
                    )

                    is PopularPostQuickActions.Share -> context.shareText(
                        R.string.posts_share_title,
                        option.post.url,
                    )

                    is PopularPostQuickActions.OpenBrowser -> context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(option.post.url)),
                    )
                }
            },
        )
    }
}

private sealed class PopularPostQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val post: Post

    data class Save(
        override val post: Post,
    ) : PopularPostQuickActions(
        title = R.string.quick_actions_save,
        icon = R.drawable.ic_save,
    )

    data class CopyUrl(
        override val post: Post,
    ) : PopularPostQuickActions(
        title = R.string.quick_actions_copy_url,
        icon = R.drawable.ic_copy,
    )

    data class Share(
        override val post: Post,
    ) : PopularPostQuickActions(
        title = R.string.quick_actions_share,
        icon = R.drawable.ic_share,
    )

    data class OpenBrowser(
        override val post: Post,
    ) : PopularPostQuickActions(
        title = R.string.quick_actions_open_in_browser,
        icon = R.drawable.ic_browser,
    )

    companion object {

        fun allOptions(
            post: Post,
        ): List<PopularPostQuickActions> = listOf(
            Save(post),
            CopyUrl(post),
            Share(post),
            OpenBrowser(post),
        )
    }
}
