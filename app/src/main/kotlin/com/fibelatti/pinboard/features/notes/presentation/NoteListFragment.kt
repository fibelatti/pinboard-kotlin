package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.notes.domain.model.Note
import kotlinx.android.synthetic.main.fragment_note_list.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class NoteListFragment @Inject constructor(
    private val noteListAdapter: NoteListAdapter
) : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "NoteListFragment"
    }

    private val appStateViewModel: AppStateViewModel by lazy {
        viewModelFactory.get<AppStateViewModel>(this)
    }
    private val noteListViewModel: NoteListViewModel by lazy {
        viewModelFactory.get<NoteListViewModel>(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_note_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        swipeToRefresh.setOnRefreshListener {
            swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshNotes)
        }

        recyclerViewNotes
            .withLinearLayoutManager()
            .adapter = noteListAdapter

        noteListAdapter.onNoteClicked = { appStateViewModel.runAction(ViewNote(it)) }

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

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.noteListContent) { content ->
            // Reset the navigate up action here since navigation to another fragment messes it up
            mainActivity?.updateTitleLayout { setNavigateUp { navigateBack() } }

            handleLoading(content.shouldLoad)

            if (content.shouldLoad) {
                noteListViewModel.getAllNotes()
            } else {
                showNotes(content.notes)
            }

            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)
        }
        error(noteListViewModel.error, ::handleError)
    }

    private fun handleLoading(loading: Boolean) {
        layoutProgressBar.visibleIf(loading, otherwiseVisibility = View.GONE)
        recyclerViewNotes.goneIf(loading)
        layoutEmptyList.goneIf(loading)
    }

    private fun showNotes(list: List<Note>) {
        if (list.isNotEmpty()) {
            recyclerViewNotes.visible()
            layoutEmptyList.gone()

            mainActivity?.updateTitleLayout {
                setNoteListTitle(getString(R.string.notes_title), list.size)
            }

            noteListAdapter.submitList(list)
        } else {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        recyclerViewNotes.gone()
        layoutEmptyList.apply {
            setIcon(R.drawable.ic_notes)
            setTitle(R.string.notes_empty_title)
            setDescription(R.string.notes_empty_description)
            visible()
        }
    }
}
