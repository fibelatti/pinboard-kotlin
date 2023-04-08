package com.fibelatti.pinboard.core.android

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.core.extension.setViewTreeOwners
import com.fibelatti.ui.theme.ExtendedTheme
import com.google.android.material.bottomsheet.BottomSheetDialog

object SelectionDialog {

    fun <T> show(
        context: Context,
        title: String,
        options: List<T>,
        optionName: (T) -> String,
        optionIcon: (T) -> Int? = { null },
        onOptionSelected: (T) -> Unit,
    ) {
        BottomSheetDialog(context).apply {
            setViewTreeOwners()

            val content = ComposeView(context).apply {
                setContent {
                    ExtendedTheme {
                        SelectionDialogContent(
                            title = title,
                            options = options,
                            optionName = optionName,
                            optionIcon = optionIcon,
                            onOptionSelected = { option ->
                                onOptionSelected(option)
                                dismiss()
                            }
                        )
                    }
                }
            }

            setContentView(content)
        }.show()
    }
}

@Composable
private fun <T> SelectionDialogContent(
    title: String,
    options: List<T>,
    optionName: (T) -> String,
    optionIcon: (T) -> Int? = { null },
    onOptionSelected: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = ExtendedTheme.typography.title,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(options.size) { index ->
                val option = options[index]

                FilledTonalButton(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = optionName(option),
                        modifier = Modifier.weight(1F),
                        textAlign = TextAlign.Center,
                    )

                    optionIcon(option)?.let {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = "",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
