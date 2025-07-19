@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.fibelatti.pinboard.features.tags.presentation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ComposeBottomSheetDialog
import com.fibelatti.pinboard.core.android.composable.RememberedEffect
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

object RenameTagDialog {

    fun show(
        context: Context,
        tag: Tag,
        onRename: (Tag, String) -> Unit,
    ) {
        ComposeBottomSheetDialog(context) {
            val keyboardController = LocalSoftwareKeyboardController.current

            RenameTagScreen(
                onRename = { newName ->
                    onRename(tag, newName)
                    keyboardController?.hide()
                    dismiss()
                },
            )
        }.show()
    }
}

@Composable
private fun RenameTagScreen(
    onRename: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val textFieldState = rememberTextFieldState()
        val focusRequester = remember { FocusRequester() }

        OutlinedTextField(
            state = textFieldState,
            modifier = Modifier
                .weight(1F)
                .focusRequester(focusRequester),
            label = { Text(text = stringResource(id = R.string.tag_filter_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onKeyboardAction = KeyboardActionHandler { onRename(textFieldState.text.toString()) },
            lineLimits = TextFieldLineLimits.SingleLine,
            contentPadding = OutlinedTextFieldDefaults.contentPadding(
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
            ),
        )

        Button(
            onClick = { onRename(textFieldState.text.toString()) },
            shapes = ExtendedTheme.defaultButtonShapes,
            modifier = Modifier.padding(bottom = 4.dp),
        ) {
            Text(text = stringResource(id = R.string.quick_actions_rename))
        }

        RememberedEffect(Unit) {
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
