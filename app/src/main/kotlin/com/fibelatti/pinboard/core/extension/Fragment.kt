package com.fibelatti.pinboard.core.extension

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> Fragment.viewBinding(): ReadWriteProperty<Fragment, T> =
    object : ReadWriteProperty<Fragment, T>, DefaultLifecycleObserver {
        private var binding: T? = null

        init {
            viewLifecycleOwnerLiveData.observe(this@viewBinding, { owner: LifecycleOwner? ->
                owner?.lifecycle?.addObserver(this)
            })
        }

        override fun onDestroy(owner: LifecycleOwner) {
            binding = null
        }

        override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
            binding ?: error("Called before onCreateView or after onDestroyView.")

        override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
            binding = value
        }
    }
