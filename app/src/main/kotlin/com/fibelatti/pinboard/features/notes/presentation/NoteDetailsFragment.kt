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
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.databinding.FragmentNoteDetailBinding
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.bottomBarHost
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.titleLayoutHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "NoteDetailsFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val noteDetailsViewModel: NoteDetailsViewModel by viewModels()

    private val binding by viewBinding(FragmentNoteDetailBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentNoteDetailBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            appStateViewModel.noteDetailContent.collect { content ->
                setupActivityViews()
                content.note.either({ getNoteDetails(content) }, ::showNote)
            }
        }
        lifecycleScope.launch {
            noteDetailsViewModel.error.collect(::handleError)
        }
    }

    private fun setupActivityViews() {
        titleLayoutHost.update {
            hideTitle()
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
