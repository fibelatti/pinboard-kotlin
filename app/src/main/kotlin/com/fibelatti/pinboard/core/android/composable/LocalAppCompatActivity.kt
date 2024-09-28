package com.fibelatti.pinboard.core.android.composable

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.fibelatti.core.android.extension.findOwner

/**
 * Provides the [AppCompatActivity] belonging to the current [LocalContext].
 *
 * Alternative API to `LocalActivity` that returns an [AppCompatActivity] instead.
 */
val LocalAppCompatActivity = compositionLocalWithComputedDefaultOf<AppCompatActivity> {
    LocalContext.currentValue.findOwner() ?: error("No AppCompatActivity found in the Context hierarchy")
}

/**
 * Alternative to [hiltViewModel] which returns an existing [ViewModel] or creates a new one scoped to the local
 * [AppCompatActivity].
 */
@Composable
inline fun <reified VM : ViewModel> hiltActivityViewModel(key: String? = null): VM = hiltViewModel(
    viewModelStoreOwner = LocalAppCompatActivity.current,
    key = key,
)
