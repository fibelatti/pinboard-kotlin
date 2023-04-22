package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.core.extension.withItemOffsetDecoration
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.databinding.FragmentNoteListBinding
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.RefreshNotes
import com.fibelatti.pinboard.features.appstate.ViewNote
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.notes.domain.model.NoteSorting
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment @Inject constructor(
    private val noteListAdapter: NoteListAdapter,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "NoteListFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
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
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.ByDateUpdatedDesc),
            )
            binding.recyclerViewNotes.scrollToPosition(0)
        }

        binding.buttonNoteSortingDateUpdatedAsc.setOnClickListener {
            binding.buttonNoteSortingDateUpdatedAsc.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.ByDateUpdatedAsc),
            )
            binding.recyclerViewNotes.scrollToPosition(0)
        }

        binding.buttonNoteSortingAtoZ.setOnClickListener {
            binding.buttonNoteSortingAtoZ.isChecked = true
            noteListAdapter.submitList(
                noteListViewModel.sort(noteListAdapter.currentList, NoteSorting.AtoZ),
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
        appStateViewModel.noteListContent
            .onEach { content ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(
                        title = MainState.TitleComponent.Visible(getString(R.string.notes_title)),
                        subtitle = MainState.TitleComponent.Gone,
                        navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                        bottomAppBar = MainState.BottomAppBarComponent.Gone,
                        floatingActionButton = MainState.FabComponent.Gone,
                    )
                }

                handleLoading(content.shouldLoad)

                if (content.shouldLoad) {
                    noteListViewModel.getAllNotes()
                } else {
                    showNotes(content.notes)
                }
            }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)

        noteListViewModel.error
            .onEach { throwable -> handleError(throwable, noteListViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
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

            mainViewModel.updateState { currentState ->
                currentState.copy(
                    title = MainState.TitleComponent.Visible(getString(R.string.notes_title)),
                    subtitle = MainState.TitleComponent.Visible(
                        resources.getQuantityString(R.plurals.notes_quantity, list.size, list.size),
                    ),
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
