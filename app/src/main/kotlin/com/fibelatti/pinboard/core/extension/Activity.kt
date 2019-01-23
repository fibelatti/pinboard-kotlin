package com.fibelatti.pinboard.core.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.fibelatti.pinboard.core.di.createInstance

inline fun <reified T : Fragment> FragmentActivity.createFragment(): Fragment =
    supportFragmentManager.fragmentFactory.createInstance<T>()
