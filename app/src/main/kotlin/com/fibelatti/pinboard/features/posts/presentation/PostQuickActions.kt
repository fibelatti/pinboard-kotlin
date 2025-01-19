package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class PostQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val post: Post
    abstract val serializedName: String

    data class ToggleReadLater(
        override val post: Post,
    ) : PostQuickActions(
        title = if (post.readLater == true) {
            R.string.quick_actions_remove_read_later
        } else {
            R.string.quick_actions_add_read_later
        },
        icon = R.drawable.ic_read_later,
    ) {

        override val serializedName: String = "TOGGLE_READ_LATER"
    }

    data class CopyTags(
        override val post: Post,
        val tags: List<Tag>,
    ) : PostQuickActions(
        title = R.string.quick_actions_copy_tags,
        icon = R.drawable.ic_tag,
    ) {

        override val serializedName: String = "COPY_TAGS"
    }

    data class PasteTags(
        override val post: Post,
        val tags: List<Tag>,
    ) : PostQuickActions(
        title = R.string.quick_actions_paste_tags,
        icon = R.drawable.ic_tag,
    ) {

        override val serializedName: String = "PASTE_TAGS"
    }

    data class Edit(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_edit,
        icon = R.drawable.ic_edit,
    ) {

        override val serializedName: String = "EDIT"
    }

    data class Delete(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_delete,
        icon = R.drawable.ic_delete,
    ) {

        override val serializedName: String = "DELETE"
    }

    data class CopyUrl(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_copy_url,
        icon = R.drawable.ic_copy,
    ) {

        override val serializedName: String = "COPY_URL"
    }

    data class Share(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_share,
        icon = R.drawable.ic_share,
    ) {

        override val serializedName: String = "SHARE"
    }

    data class ExpandDescription(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_expand_description,
        icon = R.drawable.ic_expand,
    ) {

        override val serializedName: String = "EXPAND_DESCRIPTION"
    }

    data class OpenBrowser(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_open_in_browser,
        icon = R.drawable.ic_browser,
    ) {

        override val serializedName: String = "OPEN_BROWSER"
    }

    data class SubmitToWayback(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_submit_to_wayback,
        icon = R.drawable.ic_send,
    ) {

        override val serializedName: String = "SUBMIT_TO_WAYBACK"
    }

    data class SearchWayback(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_search_wayback,
        icon = R.drawable.ic_search,
    ) {

        override val serializedName: String = "SEARCH_WAYBACK"
    }

    companion object {

        fun allOptions(
            post: Post,
            tagsClipboard: List<Tag> = emptyList(),
        ): List<PostQuickActions> = buildList {
            add(ToggleReadLater(post))

            if (!post.tags.isNullOrEmpty()) {
                add(CopyTags(post, post.tags))
            }

            if (tagsClipboard.isNotEmpty()) {
                add(PasteTags(post, tagsClipboard))
            }

            add(Edit(post))
            add(Delete(post))
            add(CopyUrl(post))
            add(Share(post))
            add(SubmitToWayback(post))
            add(SearchWayback(post))

            if (post.displayDescription.isNotBlank()) {
                add(ExpandDescription(post))
            }

            add(OpenBrowser(post))
        }
    }
}
