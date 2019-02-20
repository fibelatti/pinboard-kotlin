package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.children
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.isKeyboardSubmit
import com.fibelatti.core.extension.textAsString
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.applyAs
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.navigateBack
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.tags.presentation.TagsAdapter
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_search_post.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_tag_list.*
import javax.inject.Inject

@Suppress("ValidFragment")
class PostSearchFragment @Inject constructor(
    private val tagsAdapter: TagsAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = PostSearchFragment::class.java.simpleName
    }

    private val navigationViewModel: NavigationViewModel by lazy {
        viewModelFactory.get<NavigationViewModel>(requireActivity())
    }
    private val tagsViewModel: TagsViewModel by lazy { viewModelFactory.get<TagsViewModel>(this) }

    private var selectedTags: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()

        tagsViewModel.getAll()
    }

    private fun setupLayout() {
        layoutRoot.animateChangingTransitions()

        editTextSearchTerm.setOnEditorActionListener { _, actionId, event ->
            if (isKeyboardSubmit(actionId, event)) editTextSearchTerm.hideKeyboard()
            return@setOnEditorActionListener true
        }

        recyclerViewTags
            .withLinearLayoutManager()
            .adapter = tagsAdapter

        tagsAdapter.onItemClicked = { navigationViewModel.updateSearchTags(it.name) }
        tagsAdapter.onEmptyFilter = { showEmptyLayout() }

        mainActivity?.updateTitleLayout {
            setTitle(R.string.search_title)
            setNavigateUp {
                hideKeyboard()
                navigateBack()
            }
        }

        mainActivity?.updateViews { bottomAppBar: BottomAppBar, fab: FloatingActionButton ->
            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_search)
                setOnMenuItemClickListener { item: MenuItem? -> handleMenuClick(item) }
            }
            fab.run {
                blink {
                    setImageResource(R.drawable.ic_search)
                    setOnClickListener {
                        navigationViewModel.search(term = editTextSearchTerm.textAsString())
                        navigateBack()
                    }
                }
            }
        }
    }

    private fun setupViewModels() {
        with(navigationViewModel) {
            observe(search) { currentSearch ->
                currentSearch.term.takeIf { it.isNotEmpty() }?.let(editTextSearchTerm::setText)

                selectedTags = if (currentSearch.tags.isNotEmpty()) {
                    textViewSelectedTagsTitle.visible()
                    currentSearch.tags.forEach(::addSelectionChip)
                    currentSearch.tags.joinToString(",")
                } else {
                    textViewSelectedTagsTitle.gone()
                    ""
                }

                tagsAdapter.filter(selectedTags)
            }
        }
        with(tagsViewModel) {
            observe(tags, ::showTags)
            observeEvent(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
                recyclerViewTags.goneIf(it)
                layoutEmptyList.goneIf(it)
            }
            error(error, ::handleError)
        }
    }

    private fun showTags(list: List<Tag>) {
        if (list.isNotEmpty()) {
            recyclerViewTags.visible()
            layoutEmptyList.gone()

            tagsAdapter.addAll(list)
            tagsAdapter.filter(selectedTags)
        } else {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        recyclerViewTags.gone()
        layoutEmptyList.apply {
            setIcon(R.drawable.ic_tag)
            setTitle(R.string.tags_empty_title)
            setDescription(R.string.tags_empty_description)
            visible()
        }
    }

    private fun handleMenuClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuItemClearSearch -> {
                navigationViewModel.clearSearch()
                navigateBack()
            }
        }

        return true
    }

    private fun addSelectionChip(value: String) {
        val chip = layoutInflater.inflate(R.layout.list_item_chip, chipGroupSelectedTags, false)
            .applyAs<View, Chip> {
                text = value
                setOnCloseIconClickListener {
                    chipGroupSelectedTags.removeView(this)
                    navigationViewModel.updateSearchTags(value, shouldRemove = true)
                }
            }

        if (chipGroupSelectedTags.children.none { (it as? Chip)?.text == value }) {
            chipGroupSelectedTags.addView(chip)
        }
    }
}
