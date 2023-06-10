package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.ui.foundation.toStableList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PopularPostsFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val popularPostsViewModel: PopularPostsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setThemedContent {
            PopularBookmarksScreen(
                appStateViewModel = appStateViewModel,
                popularPostsViewModel = popularPostsViewModel,
                onBookmarkLongClicked = ::showQuickActionsDialogs,
            )
        }

        setupViewModels()
    }

    private fun showQuickActionsDialogs(post: Post) {
        SelectionDialog.show(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = PopularPostQuickActions.allOptions(post).toStableList(),
            optionName = { option -> getString(option.title) },
            optionIcon = PopularPostQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is PopularPostQuickActions.Save -> popularPostsViewModel.saveLink(option.post)
                    is PopularPostQuickActions.CopyUrl -> requireContext().copyToClipboard(post.title, post.url)
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

    private fun setupViewModels() {
        appStateViewModel.popularPostsContent
            .onEach {
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getString(R.string.popular_title)),
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                        bottomAppBar = MainState.BottomAppBarComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)

        popularPostsViewModel.error
            .onEach { throwable -> handleError(throwable, popularPostsViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    companion object {

        @JvmStatic
        val TAG: String = "PopularPostsFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
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
