package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentTagsBinding
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.mainActivity
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TagsFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "TagsFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val tagsViewModel by viewModel { viewModelProvider.tagsViewModel() }

    private var binding by viewBinding<FragmentTagsBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTagsBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        binding.tagListLayout.setAdapter(tagsAdapter) { appStateViewModel.runAction(PostsForTag(it)) }
        binding.tagListLayout.setOnRefreshListener { appStateViewModel.runAction(RefreshTags) }
        binding.tagListLayout.setSortingClickListener(tagsViewModel::sortTags)
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            appStateViewModel.tagListContent.collect { content ->
                setupActivityViews()

                if (content.shouldLoad) {
                    binding.tagListLayout.showLoading()
                    tagsViewModel.getAll(TagsViewModel.Source.MENU)
                } else {
                    tagsViewModel.sortTags(content.tags, binding.tagListLayout.getCurrentTagSorting())
                }

                binding.layoutOfflineAlert.root.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
            }
        }
        lifecycleScope.launch {
            tagsViewModel.tags.collect(binding.tagListLayout::showTags)
        }
        lifecycleScope.launch {
            tagsViewModel.error.collect(::handleError)
        }
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.tags_title)
            hideSubTitle()
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                menu.clear()
                gone()
            }
            fab.hide()
        }
    }
}
