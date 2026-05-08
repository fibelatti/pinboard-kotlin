package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Browser
import com.fibelatti.pinboard.core.android.icons.Copy
import com.fibelatti.pinboard.core.android.icons.Delete
import com.fibelatti.pinboard.core.android.icons.Edit
import com.fibelatti.pinboard.core.android.icons.Expand
import com.fibelatti.pinboard.core.android.icons.ReadLater
import com.fibelatti.pinboard.core.android.icons.Search
import com.fibelatti.pinboard.core.android.icons.Send
import com.fibelatti.pinboard.core.android.icons.Share
import com.fibelatti.pinboard.core.android.icons.Tag
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class PostQuickActions(
    @StringRes val title: Int,
    val icon: ImageVector,
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
        icon = AppIcons.ReadLater,
    ) {

        override val serializedName: String = "TOGGLE_READ_LATER"
    }

    data class CopyTags(
        override val post: Post,
        val tags: List<Tag>,
    ) : PostQuickActions(
        title = R.string.quick_actions_copy_tags,
        icon = AppIcons.Tag,
    ) {

        override val serializedName: String = "COPY_TAGS"
    }

    data class PasteTags(
        override val post: Post,
        val tags: List<Tag>,
    ) : PostQuickActions(
        title = R.string.quick_actions_paste_tags,
        icon = AppIcons.Tag,
    ) {

        override val serializedName: String = "PASTE_TAGS"
    }

    data class Edit(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_edit,
        icon = AppIcons.Edit,
    ) {

        override val serializedName: String = "EDIT"
    }

    data class Delete(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_delete,
        icon = AppIcons.Delete,
    ) {

        override val serializedName: String = "DELETE"
    }

    data class CopyUrl(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_copy_url,
        icon = AppIcons.Copy,
    ) {

        override val serializedName: String = "COPY_URL"
    }

    data class Share(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_share,
        icon = AppIcons.Share,
    ) {

        override val serializedName: String = "SHARE"
    }

    data class ExpandDescription(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_expand_description,
        icon = AppIcons.Expand,
    ) {

        override val serializedName: String = "EXPAND_DESCRIPTION"
    }

    data class OpenBrowser(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_open_in_browser,
        icon = AppIcons.Browser,
    ) {

        override val serializedName: String = "OPEN_BROWSER"
    }

    data class SearchWayback(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_search_wayback,
        icon = AppIcons.Search,
    ) {

        override val serializedName: String = "SEARCH_WAYBACK"
    }

    data class SendToWayback(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_submit_to_wayback,
        icon = AppIcons.Send,
    ) {

        override val serializedName: String = "SUBMIT_TO_WAYBACK"
    }

    data class SendToArchiveToday(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_submit_to_archive_today,
        icon = AppIcons.Send,
    ) {

        override val serializedName: String = "SEND_TO_ARCHIVE_TODAY"
    }

    data class SendToGhostArchive(
        override val post: Post,
    ) : PostQuickActions(
        title = R.string.quick_actions_submit_to_ghost_archive,
        icon = AppIcons.Send,
    ) {

        override val serializedName: String = "SEND_TO_GHOST_ARCHIVE"
    }

    companion object {

        fun allOptions(
            post: Post,
            tagsClipboard: List<Tag> = emptyList(),
        ): List<PostQuickActions> = buildList {
            if (post.displayDescription.isNotBlank()) {
                add(ExpandDescription(post))
            }

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

            add(SearchWayback(post))
            add(SendToWayback(post))
            add(SendToArchiveToday(post))
            add(SendToGhostArchive(post))

            add(OpenBrowser(post))
        }
    }
}
