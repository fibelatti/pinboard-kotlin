package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.core.extension.showKeyboard
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.databinding.FragmentSearchPostBinding
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
import com.fibelatti.pinboard.features.tags.presentation.TagList
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.ui.components.ChipGroup
import com.fibelatti.ui.components.SingleLineChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class PostSearchFragment @Inject constructor() : BaseFragment() {

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

        binding.tagListComposeView.setContent {
            AppTheme {
                TagList(
                    tagsViewModel = tagsViewModel,
                    onTagClicked = { appStateViewModel.runAction(AddSearchTag(it)) },
                    onPullToRefresh = { appStateViewModel.runAction(RefreshSearchTags) }
                )
            }
        }

        setupActivityViews()

        binding.root.doOnLayout {
            binding.editTextSearchTerm.requestFocus()
            binding.editTextSearchTerm.showKeyboard()
        }
    }

    private fun setupViewModels() {
        appStateViewModel.searchContent
            .onEach { content ->
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
                binding.chipGroupSelectedTags.isVisible = hasSelectedTags
                binding.chipGroupSelectedTags.setContent {
                    SelectedTags(content.searchParameters.tags)
                }

                if (content.shouldLoadTags) {
                    tagsViewModel.getAll(TagsViewModel.Source.SEARCH)
                } else {
                    tagsViewModel.sortTags(content.availableTags)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        tagsViewModel.state
            .onEach {
                val imeVisible = ViewCompat.getRootWindowInsets(requireView())
                    ?.isVisible(WindowInsetsCompat.Type.ime()) ?: false

                binding.textInputLayoutSearchTerm.isGone = imeVisible && it.isSearching
                binding.textViewSearchTermCaveat.isGone = imeVisible && it.isSearching
            }
            .launchInAndFlowWith(viewLifecycleOwner)
        tagsViewModel.error
            .onEach(::handleError)
            .launchInAndFlowWith(viewLifecycleOwner)

        searchPostViewModel.queryResultSize
            .onEach { querySize ->
                binding.textViewQueryResultSize.text = getString(R.string.search_result_size, querySize)
            }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    @Composable
    private fun SelectedTags(tags: List<Tag>) {
        AppTheme {
            SingleLineChipGroup(
                items = tags.map {
                    ChipGroup.Item(
                        text = it.name,
                        icon = painterResource(id = R.drawable.ic_close)
                    )
                },
                onItemClick = {},
                onItemIconClick = { item ->
                    appStateViewModel.runAction(RemoveSearchTag(tags.first { it.name == item.text }))
                },
                itemColors = ChipGroup.colors(
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                itemTextStyle = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                )
            )
        }
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
            binding.textInputLayoutSearchTerm.isGone = imeVisible && tagsViewModel.state.value.isSearching
            binding.textViewSearchTermCaveat.isGone = imeVisible && tagsViewModel.state.value.isSearching
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemClearSearch -> appStateViewModel.runAction(ClearSearch)
        }

        return true
    }
}
