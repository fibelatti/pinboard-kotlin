package com.fibelatti.pinboard.features.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fibelatti.pinboard.R

/**
 * Hosts the side effects of the export flow: prompting for the destination file, reporting the
 * outcome and blocking interaction while the file is being prepared.
 *
 * Only one instance should be composed at a time, otherwise the destination prompt is launched more
 * than once for the same file.
 */
@Composable
fun ExportBookmarksEffects(
    state: ExportBookmarksViewModel.State,
    onDestinationSelect: (Uri?) -> Unit,
    onMessageDismiss: (ExportBookmarksViewModel.State.Message) -> Unit,
) {
    val localContext: Context = LocalContext.current
    val currentOnMessageDismiss: (ExportBookmarksViewModel.State.Message) -> Unit by rememberUpdatedState(
        newValue = onMessageDismiss,
    )

    val saveFileLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result: ActivityResult ->
        onDestinationSelect(result.data?.data)
    }

    SideEffect(state.preparedFile) {
        if (state.preparedFile != null) {
            val intent: Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/html"
                putExtra(Intent.EXTRA_TITLE, state.preparedFile.name)
            }
            saveFileLauncher.launch(intent)
        }
    }

    SideEffect(state.messages) {
        state.messages.firstOrNull()?.let { message ->
            val messageRes = when (message) {
                ExportBookmarksViewModel.State.Message.EXPORT_SUCCESS -> R.string.export_feedback_success
                ExportBookmarksViewModel.State.Message.EXPORT_FAILURE -> R.string.export_feedback_failure
            }

            Toast.makeText(localContext, messageRes, Toast.LENGTH_SHORT).show()
            currentOnMessageDismiss(message)
        }
    }

    if (state.isProcessing) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
