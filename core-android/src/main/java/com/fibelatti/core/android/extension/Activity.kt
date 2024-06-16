package com.fibelatti.core.android.extension

import android.app.Activity
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat
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
inline fun <reified T : Fragment> FragmentActivity.createFragment(): T =
    supportFragmentManager.fragmentFactory.instantiate(T::class.java.classLoader!!, T::class.java.name) as T

/**
 * Creates an intent using [ShareCompat] to share the given [text] to other apps.
 *
 * @param title the [StringRes] of the title to be displayed in the chooser
 * @param text the text to be shared
 */
fun Activity.shareText(@StringRes title: Int, text: String) {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle(title)
        .setText(text)
        .startChooser()
}

/**
 * Creates an intent using [ShareCompat] to share the given [text] to other apps.
 *
 * @param title the text of the title to be displayed in the chooser
 * @param text the text to be shared
 */
fun Activity.shareText(title: String, text: String) {
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle(title)
        .setText(text)
        .startChooser()
}
