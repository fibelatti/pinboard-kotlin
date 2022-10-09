package com.fibelatti.pinboard.features.tags.presentation

import android.content.Context
import android.view.inputmethod.EditorInfo
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.core.extension.onActionOrKeyboardSubmit
import com.fibelatti.pinboard.databinding.FragmentRenameTagBinding
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.google.android.material.bottomsheet.BottomSheetDialog

object RenameTagDialog {

    fun show(
        context: Context,
        tag: Tag,
        onRename: (Tag, String) -> Unit,
    ) {
        BottomSheetDialog(context).apply {
            val dialogLayout = FragmentRenameTagBinding.inflate(layoutInflater)
            setContentView(dialogLayout.root)

            val action = {
                onRename(tag, dialogLayout.editTextNewName.text.toString())
                dialogLayout.root.hideKeyboard()
                dismiss()
            }

            dialogLayout.editTextNewName.onActionOrKeyboardSubmit(EditorInfo.IME_ACTION_DONE) { action() }
            dialogLayout.buttonRename.setOnClickListener { action() }
        }.show()
    }
}
