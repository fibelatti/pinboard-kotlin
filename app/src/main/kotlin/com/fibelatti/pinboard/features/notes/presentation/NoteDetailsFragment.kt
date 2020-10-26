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
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentNoteDetailBinding
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.notes.domain.model.Note
import javax.inject.Inject

class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "NoteDetailsFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val noteDetailsViewModel by viewModel { viewModelProvider.noteDetailsViewModel() }

    private var binding by viewBinding<FragmentNoteDetailBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentNoteDetailBinding.inflate(inflater, container, false).run {
        binding = this
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.noteDetailContent) { content ->
            setupActivityViews()
            binding.layoutOfflineAlert.root.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)

            content.note.either({ getNoteDetails(content) }, ::showNote)
        }
        viewLifecycleOwner.observe(noteDetailsViewModel.error, ::handleError)
    }

    private fun setupActivityViews() {
        mainActivity?.updateTitleLayout {
            hideTitle()
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

    private fun getNoteDetails(content: NoteDetailContent) {
        binding.layoutProgressBar.root.visible()
        binding.layoutDetailsRoot.gone()
        noteDetailsViewModel.getNoteDetails(content.id)
    }

    private fun showNote(note: Note) {
        binding.layoutProgressBar.root.gone()
        binding.layoutDetailsRoot.visible()

        binding.textViewNoteTitle.text = note.title
        binding.textViewNoteSavedDate.text = getString(R.string.notes_saved_at, note.createdAt)
        binding.textViewNoteUpdatedDate.visibleIf(
            predicate = note.updatedAt != note.createdAt,
            text = getString(R.string.notes_updated_at, note.updatedAt),
            otherwiseVisibility = View.GONE
        )
        binding.textViewText.text = note.text
    }
}
