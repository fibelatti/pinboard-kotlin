package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.core.extension.withLinearLayoutManager
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentNoteListBinding
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import javax.inject.Inject

class NoteListFragment @Inject constructor(
    private val noteListAdapter: NoteListAdapter
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "NoteListFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val noteListViewModel by viewModel { viewModelProvider.noteListViewModel() }

    private var binding by viewBinding<FragmentNoteListBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentNoteListBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
        binding.buttonNoteSortingDateUpdatedDesc.isChecked = true

        binding.buttonNoteSortingDateUpdatedDesc.setOnClickListener {
            binding.buttonNoteSortingDateUpdatedDesc.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.ByDateUpdatedDesc)
            )
            binding.recyclerViewNotes.scrollToPosition(0)
        }

        binding.buttonNoteSortingDateUpdatedAsc.setOnClickListener {
            binding.buttonNoteSortingDateUpdatedAsc.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.ByDateUpdatedAsc)
            )
            binding.recyclerViewNotes.scrollToPosition(0)
        }

        binding.buttonNoteSortingAtoZ.setOnClickListener {
            binding.buttonNoteSortingAtoZ.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.AtoZ)
            )
            binding.recyclerViewNotes.scrollToPosition(0)
        }

        binding.swipeToRefresh.setOnRefreshListener {
            binding.buttonNoteSortingDateUpdatedDesc.isChecked = true
            binding.swipeToRefresh.isRefreshing = false
            appStateViewModel.runAction(RefreshNotes)
        }

        binding.recyclerViewNotes
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

            binding.layoutOfflineAlert.root.goneIf(
                content.isConnected,
                otherwiseVisibility = View.VISIBLE
            )
        }
        observe(noteListViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        // Reset the navigate up action here since navigation to another fragment messes it up
        mainActivity?.updateTitleLayout {
            setTitle(R.string.notes_title)
            hideSubTitle()
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
        binding.layoutProgressBar.root.visibleIf(loading, otherwiseVisibility = View.GONE)
        binding.buttonGroupNoteSorting.goneIf(loading)
        binding.recyclerViewNotes.goneIf(loading)
        binding.layoutEmptyList.goneIf(loading)
    }

    private fun showNotes(list: List<Note>) {
        if (list.isNotEmpty()) {
            binding.buttonGroupNoteSorting.visible()
            binding.recyclerViewNotes.visible()
            binding.layoutEmptyList.gone()

            mainActivity?.updateTitleLayout {
                setTitle(getString(R.string.notes_title))
                setSubTitle(
                    resources.getQuantityString(
                        R.plurals.notes_quantity,
                        list.size,
                        list.size
                    )
                )
            }

            noteListAdapter.submitList(list)
        } else {
            showEmptyLayout()
        }
    }

    private fun showEmptyLayout() {
        binding.buttonGroupNoteSorting.gone()
        binding.recyclerViewNotes.gone()
        binding.layoutEmptyList.apply {
            setIcon(R.drawable.ic_notes)
            setTitle(R.string.notes_empty_title)
            setDescription(R.string.notes_empty_description)
            visible()
        }
    }
}
