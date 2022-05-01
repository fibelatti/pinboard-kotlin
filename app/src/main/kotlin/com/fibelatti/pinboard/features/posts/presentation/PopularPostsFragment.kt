package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentPopularPostsBinding
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PopularPostsFragment @Inject constructor(
    private val popularPostsAdapter: PopularPostsAdapter,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PopularPostsFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
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
        binding.layoutOfflineAlert.buttonRetryConnection.setOnClickListener {
            appStateViewModel.runAction(RefreshPopular)
        }

        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshPopular)
        }

        binding.recyclerViewPosts.withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = popularPostsAdapter

        popularPostsAdapter.onItemClicked = { appStateViewModel.runAction(ViewPost(it)) }
        popularPostsAdapter.quickActionsCallback =
            object : PopularPostsAdapter.QuickActionsCallback {

                override fun onShareClicked(item: Post) {
                    requireActivity().shareText(R.string.posts_share_title, item.url)
                }

                override fun onSaveClicked(item: Post) {
                    popularPostsViewModel.saveLink(item)
                }
            }
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            appStateViewModel.popularPostsContent.collect { content ->
                mainActivity?.updateTitleLayout {
                    setTitle(R.string.popular_title)
                    hideSubTitle()
                    setNavigateUp { navigateBack() }
                }

                mainActivity?.updateViews { bottomAppBar, fab ->
                    bottomAppBar.run {
                        navigationIcon = null
                        menu.clear()
                        gone()
                    }
                    fab.hide()
                }

                if (content.shouldLoad) {
                    binding.layoutProgressBar.root.visible()
                    binding.recyclerViewPosts.gone()
                    binding.layoutEmptyList.gone()
                    popularPostsViewModel.getPosts()
                } else {
                    showPosts(content)
                }

                binding.layoutOfflineAlert.root.goneIf(
                    content.isConnected,
                    otherwiseVisibility = View.VISIBLE
                )
            }
        }
        lifecycleScope.launch {
            popularPostsViewModel.loading.collect {
                binding.layoutProgressBar.root.visibleIf(it, otherwiseVisibility = View.GONE)
                binding.recyclerViewPosts.goneIf(it, otherwiseVisibility = View.VISIBLE)
            }
        }
        lifecycleScope.launch {
            popularPostsViewModel.saved.collect { binding.root.showBanner(getString(R.string.posts_saved_feedback)) }
        }
        lifecycleScope.launch {
            popularPostsViewModel.error.collect(::handleError)
        }
    }

    private fun showPosts(content: PopularPostsContent) {
        binding.layoutProgressBar.root.gone()

        if (content.posts.isEmpty()) {
            binding.recyclerViewPosts.gone()
            binding.layoutEmptyList.apply {
                visible()
                setTitle(R.string.posts_empty_title)
                setDescription(R.string.posts_empty_description)
            }
            return
        }

        binding.layoutEmptyList.gone()
        binding.recyclerViewPosts.visible()
        popularPostsAdapter.submitList(content.posts)
    }
}
