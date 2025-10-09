package com.fibelatti.pinboard.features.posts.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.composable.SelectionDialogBottomSheet
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.components.AppSheetState
import com.fibelatti.ui.components.bottomSheetData

@Composable
fun PopularBookmarkQuickActionsBottomSheet(
    sheetState: AppSheetState,
    onSave: (Post) -> Unit,
) {
    val post: Post = sheetState.bottomSheetData() ?: return
    val localContext = LocalContext.current
    val localResources = LocalResources.current
    val localUriHandler = LocalUriHandler.current

    SelectionDialogBottomSheet(
        sheetState = sheetState,
        title = stringResource(R.string.quick_actions_title),
        options = PopularPostQuickActions.allOptions(post),
        optionName = { option -> localResources.getString(option.title) },
        optionIcon = PopularPostQuickActions::icon,
        onOptionSelected = { option ->
            when (option) {
                is PopularPostQuickActions.Save -> onSave(option.post)

                is PopularPostQuickActions.CopyUrl -> localContext.copyToClipboard(
                    label = post.displayTitle,
                    text = post.url,
                )

                is PopularPostQuickActions.Share -> localContext.shareText(
                    R.string.posts_share_title,
                    option.post.url,
                )

                is PopularPostQuickActions.OpenBrowser -> localUriHandler.openUri(option.post.url)
            }
        },
    )
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
