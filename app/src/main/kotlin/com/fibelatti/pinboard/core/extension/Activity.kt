package com.fibelatti.pinboard.core.extension

import android.app.Activity
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.fibelatti.pinboard.core.di.createInstance

inline fun <reified T : Fragment> FragmentActivity.createFragment(): Fragment =
    supportFragmentManager.fragmentFactory.createInstance<T>()

fun Activity.shareText(@StringRes title: Int, text: String) {
    ShareCompat.IntentBuilder.from(this)
        .setType("text/plain")
        .setChooserTitle(title)
        .setText(text)
        .startChooser()
}
