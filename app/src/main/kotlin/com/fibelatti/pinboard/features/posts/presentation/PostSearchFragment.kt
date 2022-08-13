package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.databinding.FragmentSearchPostBinding
import com.fibelatti.pinboard.databinding.ListItemChipBinding
import com.fibelatti.pinboard.features.BottomBarHost.Companion.bottomBarHost
import com.fibelatti.pinboard.features.TitleLayoutHost.Companion.titleLayoutHost
import com.fibelatti.pinboard.features.appstate.AddSearchTag
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.RefreshSearchTags
import com.fibelatti.pinboard.features.appstate.RemoveSearchTag
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.appstate.SetTerm
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagsAdapter
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class PostSearchFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PostSearchFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val searchPostViewModel: SearchPostViewModel by viewModels()
    private val tagsViewModel: TagsViewModel by viewModels()

    private val binding by viewBinding(FragmentSearchPostBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentSearchPostBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        handleKeyboardVisibility()
        binding.root.animateChangingTransitions()

        binding.editTextSearchTerm.doAfterTextChanged { editable ->
            appStateViewModel.runAction(SetTerm(editable.toString()))
        }

        binding.editTextSearchTerm.onActionOrKeyboardSubmit(EditorInfo.IME_ACTION_SEARCH) {
            hideKeyboard()
            appStateViewModel.runAction(Search(textAsString()))
        }

        binding.tagListLayout.setAdapter(tagsAdapter) { appStateViewModel.runAction(AddSearchTag(it)) }
        binding.tagListLayout.setOnRefreshListener { appStateViewModel.runAction(RefreshSearchTags) }
        binding.tagListLayout.setSortingClickListener(tagsViewModel::sortTags)
        binding.tagListLayout.setInputFocusChangeListener { hasFocus ->
            val imeVisible = ViewCompat.getRootWindowInsets(requireView())
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: false

            binding.textInputLayoutSearchTerm.isGone = hasFocus && imeVisible
            binding.textViewSearchTermCaveat.isGone = hasFocus && imeVisible
        }

        setupActivityViews()
    }

    private fun setupViewModels() {
        appStateViewModel.searchContent.onEach { content ->
            if (binding.editTextSearchTerm.textAsString() != content.searchParameters.term) {
                binding.editTextSearchTerm.setText(content.searchParameters.term)
                binding.editTextSearchTerm.setSelection(content.searchParameters.term.length)
            }

            binding.textViewQueryResultSize.isVisible = content.searchParameters.isActive()

            if (content.searchParameters.isActive()) {
                searchPostViewModel.searchParametersChanged(content.searchParameters)
            }

            val hasSelectedTags = content.searchParameters.tags.isNotEmpty()
            binding.textViewSelectedTagsTitle.isVisible = hasSelectedTags
            binding.layoutChipContainer.isVisible = hasSelectedTags
            binding.chipGroupSelectedTags.removeAllViews()

            for (tag in content.searchParameters.tags) {
                binding.chipGroupSelectedTags.addView(createTagChip(tag))
            }

            if (content.shouldLoadTags) {
                binding.tagListLayout.showLoading()
                tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
            } else {
                tagsViewModel.sortTags(content.availableTags, binding.tagListLayout.getCurrentTagSorting())
            }
        }.launchIn(lifecycleScope)

        tagsViewModel.tags.onEach(binding.tagListLayout::showTags).launchIn(lifecycleScope)
        tagsViewModel.error.onEach(::handleError).launchIn(lifecycleScope)

        searchPostViewModel.queryResultSize.onEach { querySize ->
            binding.textViewQueryResultSize.text = getString(R.string.search_result_size, querySize)
        }.launchIn(lifecycleScope)
    }

    private fun setupActivityViews() {
        titleLayoutHost.update {
            setTitle(R.string.search_title)
            hideSubTitle()
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        bottomBarHost.update { bottomAppBar, fab ->
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

    private fun handleKeyboardVisibility() {
        binding.root.doOnApplyWindowInsets { _, insets, _, _ ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            binding.textInputLayoutSearchTerm.isGone = imeVisible && binding.tagListLayout.isInputFocused
            binding.textViewSearchTermCaveat.isGone = imeVisible && binding.tagListLayout.isInputFocused
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemClearSearch -> appStateViewModel.runAction(ClearSearch)
        }

        return true
    }

    private fun createTagChip(value: Tag): View = ListItemChipBinding.inflate(
        LayoutInflater.from(binding.chipGroupSelectedTags.context),
        binding.chipGroupSelectedTags,
        false
    ).root.apply {
        setValue(value)
        setOnCloseIconClickListener { appStateViewModel.runAction(RemoveSearchTag(value)) }
    }
}
