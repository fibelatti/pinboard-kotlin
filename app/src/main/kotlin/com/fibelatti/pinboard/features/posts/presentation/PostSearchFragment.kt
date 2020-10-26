package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.inflate
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.onKeyboardSubmit
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentSearchPostBinding
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagsAdapter
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import javax.inject.Inject

class PostSearchFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PostSearchFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val tagsViewModel by viewModel { viewModelProvider.tagsViewModel() }

    private var binding by viewBinding<FragmentSearchPostBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentSearchPostBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        binding.root.animateChangingTransitions()

        binding.editTextSearchTerm.onKeyboardSubmit { binding.editTextSearchTerm.hideKeyboard() }

        binding.tagListLayout.setAdapter(tagsAdapter) { appStateViewModel.runAction(AddSearchTag(it)) }
        binding.tagListLayout.setOnRefreshListener { appStateViewModel.runAction(RefreshSearchTags) }
        binding.tagListLayout.setSortingClickListener(tagsViewModel::sortTags)
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.searchContent) { content ->
            setupActivityViews()
            binding.editTextSearchTerm.setText(content.searchParameters.term)

            if (content.searchParameters.tags.isNotEmpty()) {
                binding.chipGroupSelectedTags.removeAllViews()
                for (tag in content.searchParameters.tags) {
                    binding.chipGroupSelectedTags.addView(createTagChip(tag))
                }

                binding.textViewSelectedTagsTitle.visible()
            } else {
                binding.textViewSelectedTagsTitle.gone()
                binding.chipGroupSelectedTags.removeAllViews()
            }

            if (content.shouldLoadTags) {
                binding.tagListLayout.showLoading()
                tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
            } else {
                tagsViewModel.sortTags(
                    content.availableTags,
                    binding.tagListLayout.getCurrentTagSorting()
                )
            }
        }
        viewLifecycleOwner.observe(tagsViewModel.tags, binding.tagListLayout::showTags)
        viewLifecycleOwner.observe(tagsViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            setTitle(R.string.search_title)
            hideSubTitle()
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_search)
                setOnMenuItemClickListener(::handleMenuClick)
            }
            fab.run {
                blink {
                    setImageResource(R.drawable.ic_search)
                    setOnClickListener {
                        appStateViewModel.runAction(Search(binding.editTextSearchTerm.textAsString()))
                    }
                }
            }
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemClearSearch -> appStateViewModel.runAction(ClearSearch)
        }

        return true
    }

    private fun createTagChip(value: Tag): View = binding.chipGroupSelectedTags
        .inflate(R.layout.list_item_chip, false)
        .applyAs<View, TagChip> {
            setValue(value)
            setOnCloseIconClickListener { appStateViewModel.runAction(RemoveSearchTag(value)) }
        }
}
