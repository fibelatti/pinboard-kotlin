/*
 * Designed and developed by 2025 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever [key1] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    key1: Any?,
    effect: () -> Unit,
) {
    remember(key1) { RememberedEffectImpl(effect = effect) }
}

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever any of [key1] and [key2] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    key1: Any?,
    key2: Any?,
    effect: () -> Unit,
) {
    remember(key1, key2) { RememberedEffectImpl(effect = effect) }
}

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever any of [key1], [key2], and [key3] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    effect: () -> Unit,
) {
    remember(key1, key2, key3) { RememberedEffectImpl(effect = effect) }
}

/**
 * `RememberEffect` is a side-effect API that executes the provided [effect] lambda when it enters
 * the composition and re-executes it whenever any of [keys] changes.
 *
 * Unlike [LaunchedEffect], `RememberEffect` does not create or launch a new coroutine scope
 * on each key change, making it a more efficient option for remembering the execution of side-effects,
 * if you don't to launch a coroutine task.
 */
@Composable
@NonRestartableComposable
fun RememberedEffect(
    vararg keys: Any?,
    effect: () -> Unit,
) {
    remember(*keys) { RememberedEffectImpl(effect = effect) }
}

/**
 * Launches the provided [effect] lambda when it enters the composition.
 */
private class RememberedEffectImpl(
    private val effect: () -> Unit,
) : RememberObserver {

    override fun onRemembered() {
        effect.invoke()
    }

    override fun onAbandoned() {
        // no-op
    }

    override fun onForgotten() {
        // no-op
    }
}
