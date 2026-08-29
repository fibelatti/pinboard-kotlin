package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import com.fibelatti.ui.components.AppSheetState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val savedStateJson: Json = Json

/**
 * Creates a [Saver] that persists a value as its JSON representation, so that saved state can hold arbitrary models
 * without them having to implement any platform-specific serialization contract.
 *
 * Decoding is lenient: state saved by a previous version of the app may no longer match the current model, and a
 * missing value is preferable to a crash while restoring. Call sites should be ready to handle a null value.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any, S> jsonSaver(serializer: KSerializer<T>): Saver<S, Any> = Saver(
    save = { value -> (value as T?)?.let { savedStateJson.encodeToString(serializer, it) } },
    restore = { saved ->
        runCatching { savedStateJson.decodeFromString(serializer, saved as String) as S }.getOrNull()
    },
)

/**
 * Remembers a JSON [Saver] for state declared as [T]?.
 */
@Composable
fun <T : Any> rememberJsonSaver(serializer: KSerializer<T>): Saver<T?, Any> = remember {
    jsonSaver(serializer)
}

/**
 * Remembers a JSON [Saver] for the data passed to [AppSheetState.showBottomSheet], which is declared as `Any?`.
 */
@Composable
fun <T : Any> rememberJsonSheetDataSaver(serializer: KSerializer<T>): Saver<Any?, Any> = remember {
    jsonSaver(serializer)
}
