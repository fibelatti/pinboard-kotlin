package com.fibelatti.pinboard.core.android

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.fibelatti.pinboard.databinding.LayoutSelectionDialogBinding
import com.fibelatti.pinboard.databinding.ListItemSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

object SelectionDialog {

    fun <T> showSelectionDialog(
        context: Context,
        title: String,
        options: List<T>,
        optionName: (T) -> String,
        onOptionSelected: (T) -> Unit,
    ) {
        BottomSheetDialog(context).apply {
            val dialogLayout = LayoutSelectionDialogBinding.inflate(layoutInflater)

            setContentView(dialogLayout.root)

            dialogLayout.selectionTitle.text = title

            for (option in options) {
                createSelectionOption(dialogLayout.layoutOptions, option, optionName, onOptionSelected)
                    .let(dialogLayout.layoutOptions::addView)
            }
        }.show()
    }

    private fun <T> BottomSheetDialog.createSelectionOption(
        parentView: ViewGroup,
        option: T,
        optionName: (T) -> String,
        onOptionSelected: (T) -> Unit,
    ): View = ListItemSelectionBinding.inflate(layoutInflater, parentView, false)
        .apply {
            root.text = optionName(option)
            root.setOnClickListener {
                onOptionSelected(option)
                dismiss()
            }
        }
        .root
}
