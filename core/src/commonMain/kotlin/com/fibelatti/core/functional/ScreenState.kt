package com.fibelatti.core.functional

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

sealed interface ScreenState<out T> {

    sealed interface Loading<T> : ScreenState<T> {

        data object FromEmpty : Loading<Nothing>

        data class FromData<T>(val previousData: T) : Loading<T>
    }

    data class Loaded<T>(val data: T) : ScreenState<T>

    data class Error(val throwable: Throwable) : ScreenState<Nothing>
}

// region Producer extensions
fun <T> MutableStateFlow<ScreenState<T>>.emitLoading() {
    val currentValue = value
    value = if (currentValue is ScreenState.Loaded) {
        ScreenState.Loading.FromData(currentValue.data)
    } else {
        ScreenState.Loading.FromEmpty
    }
}

fun <T> MutableStateFlow<ScreenState<T>>.emitLoaded(data: T) {
    value = ScreenState.Loaded(data)
}

fun <T> MutableStateFlow<ScreenState<T>>.emitError(throwable: Throwable) {
    value = ScreenState.Error(throwable)
}
// endregion Producer extensions

// region Consumer extensions
inline fun <reified T> Flow<ScreenState<T>>.onScreenState(
    noinline onLoading: suspend (previousData: T?) -> Unit = {},
    noinline onLoaded: suspend (data: T) -> Unit = {},
    noinline onError: suspend (Throwable) -> Unit = {},
): Flow<ScreenState<T>> = onEach { state ->
    when (state) {
        is ScreenState.Loading.FromEmpty, is ScreenState.Loading.FromData -> {
            onLoading((state as? ScreenState.Loading.FromData)?.previousData)
        }

        is ScreenState.Loaded -> onLoaded(state.data)
        is ScreenState.Error -> onError(state.throwable)
    }
}

inline fun <reified T> Flow<ScreenState<T>>.onLoadingState(
    noinline action: suspend (previousData: T?) -> Unit,
): Flow<ScreenState<T>> = onEach { state ->
    if (state is ScreenState.Loading) {
        action((state as? ScreenState.Loading.FromData)?.previousData)
    }
}

inline fun <reified T : Any> Flow<ScreenState<T>>.onLoadedState(
    noinline action: suspend (data: T) -> Unit,
): Flow<ScreenState<T>> = onEach { state ->
    if (state is ScreenState.Loaded) {
        action(state.data)
    }
}

fun Flow<ScreenState<*>>.onErrorState(
    action: suspend (Throwable) -> Unit,
): Flow<ScreenState<*>> = onEach { state ->
    if (state is ScreenState.Error) {
        action(state.throwable)
    }
}
// endregion Consumer extensions
