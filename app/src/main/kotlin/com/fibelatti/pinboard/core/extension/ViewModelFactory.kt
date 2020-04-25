@file:Suppress("UNCHECKED_CAST")

package com.fibelatti.pinboard.core.extension

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> FragmentActivity.viewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    factoryProducer(provider)
}

inline fun <reified T : ViewModel> Fragment.viewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    factoryProducer(provider)
}

inline fun <reified T : ViewModel> Fragment.activityViewModel(
    crossinline provider: () -> T
) = activityViewModels<T> {
    factoryProducer(provider)
}

inline fun <reified T : ViewModel> factoryProducer(
    crossinline provider: () -> T
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = provider() as T
}
