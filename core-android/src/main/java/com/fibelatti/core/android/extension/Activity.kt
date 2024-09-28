package com.fibelatti.core.android.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory

/**
 * Shorthand function to create an instance of [Fragment] with type [T] using the [FragmentFactory]
 * of this [FragmentActivity].
 *
 * @param T the type of the [Fragment] to be created
 *
 * @return the new instance
 */
public inline fun <reified T : Fragment> FragmentActivity.createFragment(): T =
    supportFragmentManager.fragmentFactory.instantiate(T::class.java.classLoader!!, T::class.java.name) as T

/**
 * Shorthand function to invoke the `onBackPressed` of this activity's `onBackPressedDispatcher`.
 */
public fun AppCompatActivity.navigateBack() {
    onBackPressedDispatcher.onBackPressed()
}
