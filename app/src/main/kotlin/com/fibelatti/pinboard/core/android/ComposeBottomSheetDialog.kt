package com.fibelatti.pinboard.core.android

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.setViewTreeOwners
import com.google.android.material.bottomsheet.BottomSheetDialog

class ComposeBottomSheetDialog(
    context: Context,
    content: @Composable BottomSheetDialog.() -> Unit,
) : BottomSheetDialog(context) {

    init {
        setViewTreeOwners()

        setContentView(
            ComposeView(context).apply {
                setThemedContent {
                    content()
                }
            },
        )
    }
}