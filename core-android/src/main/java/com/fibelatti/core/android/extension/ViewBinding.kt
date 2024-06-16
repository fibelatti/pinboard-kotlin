package com.fibelatti.core.android.extension

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }

public fun <T : ViewBinding> Fragment.viewBinding(
    factory: (View) -> T,
): ReadOnlyProperty<Fragment, T> = object : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null
    private val lifecycleObserver = object : DefaultLifecycleObserver {

        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            binding = null
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T = binding ?: factory(requireView()).also {
        if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            viewLifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            binding = it
        }
    }
}
