package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.notes.domain.model.Note
import kotlinx.android.synthetic.main.fragment_note_detail.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class NoteDetailsFragment @Inject constructor() : BaseFragment(R.layout.fragment_note_detail) {

    companion object {
        @JvmStatic
        val TAG: String = "NoteDetailsFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val noteDetailsViewModel by viewModel { viewModelProvider.noteDetailsViewModel() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.noteDetailContent) { content ->
            setupActivityViews()
            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)

            content.note.either({ getNoteDetails(content) }, ::showNote)
        }
        viewLifecycleOwner.observe(noteDetailsViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            hideTitle()
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

    private fun getNoteDetails(content: NoteDetailContent) {
        layoutProgressBar.visible()
        layoutDetailsRoot.gone()
        noteDetailsViewModel.getNoteDetails(content.id)
    }

    private fun showNote(note: Note) {
        layoutProgressBar.gone()
        layoutDetailsRoot.visible()

        textViewNoteTitle.text = note.title
        textViewNoteSavedDate.text = getString(R.string.notes_saved_at, note.createdAt)
        textViewNoteUpdatedDate.visibleIf(
            predicate = note.updatedAt != note.createdAt,
            text = getString(R.string.notes_updated_at, note.updatedAt),
            otherwiseVisibility = View.GONE
        )
        textViewText.text = note.text
    }
}
