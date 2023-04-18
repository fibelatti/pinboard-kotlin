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
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.databinding.FragmentNoteDetailBinding
import com.fibelatti.pinboard.features.MainState
import com.fibelatti.pinboard.features.MainViewModel
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.notes.domain.model.Note
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "NoteDetailsFragment"

        private val ACTION_ID = UUID.randomUUID().toString()
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val noteDetailsViewModel: NoteDetailsViewModel by viewModels()

    private val binding by viewBinding(FragmentNoteDetailBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentNoteDetailBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.updateState { currentState ->
            currentState.copy(
                title = MainState.TitleComponent.Gone,
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(ACTION_ID),
                bottomAppBar = MainState.BottomAppBarComponent.Gone,
                floatingActionButton = MainState.FabComponent.Gone,
            )
        }

        setupViewModels()
    }

    private fun setupViewModels() {
        appStateViewModel.noteDetailContent
            .onEach { content -> content.note.either({ getNoteDetails(content) }, ::showNote) }
            .launchInAndFlowWith(viewLifecycleOwner)

        mainViewModel.navigationClicks(ACTION_ID)
            .onEach { navigateBack() }
            .launchInAndFlowWith(viewLifecycleOwner)

        noteDetailsViewModel.error
            .onEach { throwable -> handleError(throwable, noteDetailsViewModel::errorHandled) }
            .launchInAndFlowWith(viewLifecycleOwner)
    }

    private fun getNoteDetails(content: NoteDetailContent) {
        binding.layoutProgressBar.root.isVisible = true
        binding.layoutDetailsRoot.isGone = true
        noteDetailsViewModel.getNoteDetails(content.id)
    }

    private fun showNote(note: Note) {
        binding.layoutProgressBar.root.isGone = true
        binding.layoutDetailsRoot.isVisible = true

        binding.textViewNoteTitle.text = note.title
        binding.textViewNoteSavedDate.text = getString(R.string.notes_saved_at, note.createdAt)
        binding.textViewNoteUpdatedDate.isVisible = note.updatedAt != note.createdAt
        binding.textViewNoteUpdatedDate.text = getString(R.string.notes_updated_at, note.updatedAt)
        binding.textViewText.text = note.text
    }
}
