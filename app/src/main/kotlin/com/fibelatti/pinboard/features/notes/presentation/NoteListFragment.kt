package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.databinding.FragmentNoteListBinding
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.bottomBarHost
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import com.fibelatti.pinboard.features.titleLayoutHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment @Inject constructor(
    private val noteListAdapter: NoteListAdapter,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "NoteListFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val noteListViewModel: NoteListViewModel by viewModels()

    private val binding by viewBinding(FragmentNoteListBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentNoteListBinding.inflate(inflater, container, false).root

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
            .withItemOffsetDecoration(R.dimen.padding_small)
            .adapter = noteListAdapter

        noteListAdapter.onNoteClicked = { appStateViewModel.runAction(ViewNote(it)) }
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            appStateViewModel.noteListContent.collect { content ->
                setupActivityViews()
                handleLoading(content.shouldLoad)

                if (content.shouldLoad) {
                    noteListViewModel.getAllNotes()
                } else {
                    showNotes(content.notes)
                }

                binding.layoutOfflineAlert.root.isGone = content.isConnected
            }
        }
        lifecycleScope.launch {
            noteListViewModel.error.collect(::handleError)
        }
    }

    private fun setupActivityViews() {
        // Reset the navigate up action here since navigation to another fragment messes it up
        titleLayoutHost.update {
            setTitle(R.string.notes_title)
            hideSubTitle()
            setNavigateUp { navigateBack() }
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

    private fun handleLoading(loading: Boolean) {
        binding.layoutProgressBar.root.isVisible = loading
        binding.buttonGroupNoteSorting.isGone = loading
        binding.recyclerViewNotes.isGone = loading
        binding.layoutEmptyList.isGone = loading
    }

    private fun showNotes(list: List<Note>) {
        if (list.isNotEmpty()) {
            binding.buttonGroupNoteSorting.isVisible = true
            binding.recyclerViewNotes.isVisible = true
            binding.layoutEmptyList.isGone = true

            titleLayoutHost.update {
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
        binding.buttonGroupNoteSorting.isGone = true
        binding.recyclerViewNotes.isGone = true
        binding.layoutEmptyList.apply {
            setIcon(R.drawable.ic_notes)
            setTitle(R.string.notes_empty_title)
            setDescription(R.string.notes_empty_description)
            isVisible = true
        }
    }
}
