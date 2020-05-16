package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.activityViewModel
import com.fibelatti.pinboard.core.extension.viewModel
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class NoteListFragment @Inject constructor(
    private val noteListAdapter: NoteListAdapter
) : BaseFragment(R.layout.fragment_note_list) {

    companion object {
        @JvmStatic
        val TAG: String = "NoteListFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val noteListViewModel by viewModel { viewModelProvider.noteListViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        buttonNoteSortingDateUpdatedDesc.isChecked = true

        buttonNoteSortingDateUpdatedDesc.setOnClickListener {
            buttonNoteSortingDateUpdatedDesc.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.ByDateUpdatedDesc)
            )
        }

        buttonNoteSortingDateUpdatedAsc.setOnClickListener {
            buttonNoteSortingDateUpdatedAsc.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.ByDateUpdatedAsc)
            )
        }

        buttonNoteSortingAtoZ.setOnClickListener {
            buttonNoteSortingAtoZ.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.AtoZ)
            )
        }

        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshNotes)
        }

        recyclerViewNotes
            .withLinearLayoutManager()
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = noteListAdapter

        noteListAdapter.onNoteClicked = { appStateViewModel.runAction(ViewNote(it)) }
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.noteListContent) { content ->
            setupActivityViews()
            handleLoading(content.shouldLoad)

            if (content.shouldLoad) {
                noteListViewModel.getAllNotes()
            } else {
                showNotes(content.notes)
            }

            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
        }
        observe(noteListViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        // Reset the navigate up action here since navigation to another fragment messes it up
        mainActivity?.updateTitleLayout {
            setTitle(R.string.notes_title)
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
    }

    private fun handleLoading(loading: Boolean) {
        layoutProgressBar.visibleIf(loading, otherwiseVisibility = View.GONE)
        buttonGroupNoteSorting.goneIf(loading)
        recyclerViewNotes.goneIf(loading)
        layoutEmptyList.goneIf(loading)
    }

    private fun showNotes(list: List<Note>) {
        if (list.isNotEmpty()) {
            buttonGroupNoteSorting.visible()
            recyclerViewNotes.visible()
            layoutEmptyList.gone()

            mainActivity?.updateTitleLayout {
                setTitle(getString(R.string.notes_title))
                setSubTitle(resources.getQuantityString(R.plurals.notes_quantity, list.size, list.size))
            }

            noteListAdapter.submitList(list)
        } else {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        buttonGroupNoteSorting.gone()
        recyclerViewNotes.gone()
        layoutEmptyList.apply {
            setIcon(R.drawable.ic_notes)
            setTitle(R.string.notes_empty_title)
            setDescription(R.string.notes_empty_description)
            visible()
        }
    }
}
