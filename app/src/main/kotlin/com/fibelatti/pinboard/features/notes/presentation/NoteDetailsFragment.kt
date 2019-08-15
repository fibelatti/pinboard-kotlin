package com.fibelatti.pinboard.features.notes.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.mainActivity
import kotlinx.android.synthetic.main.fragment_note_detail.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class NoteDetailsFragment @Inject constructor() : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "NoteDetailsFragment"
    }

    private val appStateViewModel: AppStateViewModel by lazy {
        viewModelFactory.get<AppStateViewModel>(this)
    }
    private val noteDetailsViewModel: NoteDetailsViewModel by lazy {
        viewModelFactory.get<NoteDetailsViewModel>(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_note_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
        setupViewModels()
    }

    private fun setupLayout() {
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

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.noteDetailContent) { content ->
            layoutOfflineAlert.goneIf(content.isConnected, otherwiseVisibility = View.VISIBLE)

            content.note.either(
                {
                    layoutProgressBar.visible()
                    layoutDetailsRoot.gone()
                    noteDetailsViewModel.getNoteDetails(content.id)
                },
                { note ->
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
            )
        }
        observe(noteDetailsViewModel.error, ::handleError)
    }
}
