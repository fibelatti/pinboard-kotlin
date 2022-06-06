package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.databinding.FragmentTagsBinding
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.bottomBarHost
import com.fibelatti.pinboard.features.titleLayoutHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TagsFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "TagsFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val tagsViewModel: TagsViewModel by viewModels()

    private val binding by viewBinding(FragmentTagsBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentTagsBinding.inflate(inflater, container, false).root

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
        titleLayoutHost.update {
            setTitle(R.string.tags_title)
            hideSubTitle()
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        bottomBarHost.update { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                menu.clear()
                isGone = true
            }
            fab.hide()
        }
    }
}
