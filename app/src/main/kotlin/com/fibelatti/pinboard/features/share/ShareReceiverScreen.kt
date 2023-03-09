package com.fibelatti.pinboard.features.share

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fibelatti.core.functional.ScreenState
import com.fibelatti.pinboard.R
import com.fibelatti.ui.preview.ThemePreviews
import com.fibelatti.ui.theme.ExtendedTheme

@Composable
fun ShareReceiverScreen(
    shareReceiverViewModel: ShareReceiverViewModel = hiltViewModel(),
) {
    val state by shareReceiverViewModel.screenState.collectAsState(initial = null)
    val currentState = state

    val icon = when (currentState) {
        is ScreenState.Error -> painterResource(id = R.drawable.ic_url_saved_error)
        is ScreenState.Loaded -> painterResource(id = R.drawable.ic_url_saved)
        else -> painterResource(id = R.drawable.ic_url_saving)
    }

    if (currentState is ScreenState.Loaded) {
        currentState.data.message?.let {
            Toast.makeText(LocalContext.current, it, Toast.LENGTH_SHORT).show()
        }
    }

    ShareReceiverScreen(icon = icon)
}

@Composable
fun ShareReceiverScreen(icon: Painter) {
    Box(modifier = Modifier.fillMaxSize()) {
        Icon(
            painter = icon,
            contentDescription = stringResource(id = R.string.cd_share_receiver_image),
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.primary,
        )
        CircularProgressIndicator(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
@ThemePreviews
private fun ShareReceiverScreenPreview() {
    ExtendedTheme {
        ShareReceiverScreen(icon = painterResource(id = R.drawable.ic_url_saving))
    }
}
