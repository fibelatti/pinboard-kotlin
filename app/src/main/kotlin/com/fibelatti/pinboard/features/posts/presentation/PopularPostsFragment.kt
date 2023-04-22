package com.fibelatti.pinboard.features.posts.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.copyToClipboard
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.databinding.FragmentPopularPostsBinding
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.posts.domain.model.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PopularPostsFragment @Inject constructor(
    private val popularPostsAdapter: PopularPostsAdapter,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PopularPostsFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val popularPostsViewModel: PopularPostsViewModel by viewModels()

    private val binding by viewBinding(FragmentPopularPostsBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentPopularPostsBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshPopular)
        }

        binding.recyclerViewPosts
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = popularPostsAdapter

        popularPostsAdapter.onItemClicked = { appStateViewModel.runAction(ViewPost(it)) }
        popularPostsAdapter.onItemLongClicked = ::showQuickActionsDialogs
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
            .onEach { content ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getString(R.string.popular_title)),
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                        bottomAppBar = MainState.BottomAppBarComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }

                if (content.shouldLoad) {
                    binding.layoutProgressBar.root.isVisible = true
                    binding.recyclerViewPosts.isGone = true
                    binding.layoutEmptyList.isGone = true
                    popularPostsViewModel.getPosts()
                } else {
                    showPosts(content)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)

        popularPostsViewModel.loading
            .onEach {
                binding.layoutProgressBar.root.isVisible = it
                binding.recyclerViewPosts.isGone = it
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        popularPostsViewModel.saved
            .onEach { binding.root.showBanner(getString(R.string.posts_saved_feedback)) }
            .launchInAndFlowWith(viewLifecycleOwner)
        popularPostsViewModel.error
            .onEach { throwable -> handleError(throwable, popularPostsViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun showPosts(content: PopularPostsContent) {
        binding.layoutProgressBar.root.isGone = true

        if (content.posts.isEmpty()) {
            binding.recyclerViewPosts.isGone = true
            binding.layoutEmptyList.apply {
                isVisible = true
                setTitle(R.string.posts_empty_title)
                setDescription(R.string.posts_empty_description)
            }
            return
        }

        binding.layoutEmptyList.isGone = true
        binding.recyclerViewPosts.isVisible = true
        popularPostsAdapter.submitList(content.posts)
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
