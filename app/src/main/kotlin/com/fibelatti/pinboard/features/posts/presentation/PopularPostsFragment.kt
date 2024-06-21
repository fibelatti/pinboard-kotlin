package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.core.android.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PopularPostsFragment : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModel()
    private val mainViewModel: MainViewModel by activityViewModel()
    private val popularPostsViewModel: PopularPostsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            PopularBookmarksScreen(
                appStateViewModel = appStateViewModel,
                mainViewModel = mainViewModel,
                popularPostsViewModel = popularPostsViewModel,
                onBackPressed = { navigateBack() },
                onError = ::handleError,
                onBookmarkLongClicked = ::showQuickActionsDialogs,
            )
        }
    }

    private fun showQuickActionsDialogs(post: Post) {
        SelectionDialog.show(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = PopularPostQuickActions.allOptions(post),
            optionName = { option -> getString(option.title) },
            optionIcon = PopularPostQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is PopularPostQuickActions.Save -> popularPostsViewModel.saveLink(option.post)

                    is PopularPostQuickActions.CopyUrl -> requireContext().copyToClipboard(
                        label = post.displayTitle,
                        text = post.url,
                    )

                    is PopularPostQuickActions.Share -> requireActivity().shareText(
                        R.string.posts_share_title,
                        option.post.url,
                    )

                    is PopularPostQuickActions.OpenBrowser -> startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(option.post.url)),
                    )
                }
            },
        )
    }

    companion object {

        @JvmStatic
        val TAG: String = "PopularPostsFragment"
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
        icon = R.drawable.ic_open_in_browser,
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
