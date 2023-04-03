package com.fibelatti.pinboard.features.posts.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.android.composable.AppTheme
import com.fibelatti.pinboard.core.extension.blink
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.features.BottomBarHost.Companion.bottomBarHost
import com.fibelatti.pinboard.features.TitleLayoutHost.Companion.titleLayoutHost
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ClearSearch
import com.fibelatti.pinboard.features.appstate.Search
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class PostSearchFragment @Inject constructor() : BaseFragment() {

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val tagsViewModel: TagsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(inflater.context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as ComposeView).setContent {
            AppTheme {
                SearchBookmarksScreen(
                    appStateViewModel = appStateViewModel,
                    tagsViewModel = tagsViewModel
                )
            }
        }

        setupActivityViews()

        tagsViewModel.error
            .onEach(::handleError)
            .launchInAndFlowWith(viewLifecycleOwner)
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
                    setOnClickListener { appStateViewModel.runAction(Search) }
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

    companion object {

        @JvmStatic
        val TAG: String = "PostSearchFragment"
    }
}
