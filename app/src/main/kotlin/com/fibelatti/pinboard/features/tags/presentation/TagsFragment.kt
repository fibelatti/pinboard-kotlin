package com.fibelatti.pinboard.features.tags.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SelectionDialog
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.databinding.FragmentTagsBinding
import com.fibelatti.pinboard.features.BottomBarHost.Companion.bottomBarHost
import com.fibelatti.pinboard.features.TitleLayoutHost.Companion.titleLayoutHost
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PostsForTag
import com.fibelatti.pinboard.features.appstate.RefreshTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
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
        binding.tagListLayout.setAdapter(
            tagsAdapter,
            onClickListener = { appStateViewModel.runAction(PostsForTag(it)) },
            onLongClickListener = ::showTagQuickActions,
        )
        binding.tagListLayout.setOnRefreshListener { appStateViewModel.runAction(RefreshTags) }
        binding.tagListLayout.setSortingClickListener(tagsViewModel::sortTags)
    }

    private fun showTagQuickActions(tag: Tag) {
        SelectionDialog.showSelectionDialog(
            context = requireContext(),
            title = getString(R.string.quick_actions_title),
            options = TagQuickActions.allOptions(tag),
            optionName = { getString(it.title) },
            optionIcon = TagQuickActions::icon,
            onOptionSelected = { option ->
                when (option) {
                    is TagQuickActions.Rename -> {
                        RenameTagDialog.show(
                            context = requireContext(),
                            tag = option.tag,
                            onRename = tagsViewModel::renameTag
                        )
                    }
                }
            }
        )
    }

    private fun setupViewModels() {
        appStateViewModel.tagListContent
            .onEach { content ->
                setupActivityViews()

                if (content.shouldLoad) {
                    binding.tagListLayout.isLoading = true
                    tagsViewModel.getAll(TagsViewModel.Source.MENU)
                } else {
                    tagsViewModel.sortTags(content.tags, binding.tagListLayout.getCurrentTagSorting())
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        tagsViewModel.tags
            .onEach(binding.tagListLayout::showTags)
            .launchInAndFlowWith(viewLifecycleOwner)
        tagsViewModel.loading
            .onEach { loading -> binding.tagListLayout.isLoading = loading }
            .launchInAndFlowWith(viewLifecycleOwner)
        tagsViewModel.error
            .onEach(::handleError)
            .launchInAndFlowWith(viewLifecycleOwner)
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

private sealed class TagQuickActions(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
) {

    abstract val tag: Tag

    data class Rename(
        override val tag: Tag,
    ) : TagQuickActions(
        title = R.string.quick_actions_rename,
        icon = R.drawable.ic_edit,
    )

    companion object {

        fun allOptions(
            tag: Tag,
        ): List<TagQuickActions> = listOf(
            Rename(tag),
        )
    }
}
