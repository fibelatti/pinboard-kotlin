package com.fibelatti.pinboard.features.tags.presentation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fibelatti.core.extension.hideKeyboard
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.setViewTreeOwners
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.bottomsheet.BottomSheetDialog

object RenameTagDialog {

    fun show(
        context: Context,
        tag: Tag,
        onRename: (Tag, String) -> Unit,
    ) {
        BottomSheetDialog(context).apply {
            setViewTreeOwners()

            setContentView(
                ComposeView(context).apply {
                    setThemedContent {
                        RenameTagScreen(
                            onRename = { newName ->
                                onRename(tag, newName)
                                hideKeyboard()
                                dismiss()
                            },
                        )
                    }
                },
            )
        }.show()
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RenameTagScreen(
    onRename: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        var newName by rememberSaveable { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        OutlinedTextField(
            value = newName,
            onValueChange = { newValue -> newName = newValue },
            modifier = Modifier
                .weight(1F)
                .focusRequester(focusRequester),
            label = { Text(text = stringResource(id = R.string.tag_filter_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { onRename(newName) },
            singleLine = true,
            maxLines = 1,
        )

        FilledTonalButton(
            onClick = { onRename(newName) },
            modifier = Modifier.padding(start = 8.dp, top = 12.dp),
        ) {
            Text(
                text = stringResource(id = R.string.quick_actions_rename),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
@ThemePreviews
private fun RenameTagScreenPreview() {
    ExtendedTheme {
        Box {
            RenameTagScreen(onRename = {})
        }
    }
}
