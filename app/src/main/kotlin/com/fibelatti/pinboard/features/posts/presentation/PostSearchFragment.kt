package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.navigateBack
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_search_post.*
import javax.inject.Inject

class PostSearchFragment @Inject constructor() : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = PostSearchFragment::class.java.simpleName
    }

    private val navigationViewModel: NavigationViewModel by lazy {
        viewModelFactory.get<NavigationViewModel>(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(com.fibelatti.pinboard.R.layout.fragment_search_post, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
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
                        // TODO
                    }
                }
            }
        }
    }

    private fun setupViewModels() {
        with(navigationViewModel) {
            observe(search) { currentSearch ->
                currentSearch.term.takeIf { it.isNotEmpty() }?.let(editTextSearchTerm::setText)
                currentSearch.tags.takeIf { it.isNotEmpty() }?.let { it.forEach(::addSelectionChip) }
            }
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
        // TODO
    }
}
