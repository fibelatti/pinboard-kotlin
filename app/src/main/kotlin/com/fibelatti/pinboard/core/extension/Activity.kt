package com.fibelatti.pinboard.core.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.core.di.createInstance

inline fun <reified T : Fragment> AppCompatActivity.createFragment(): Fragment =
    supportFragmentManager.fragmentFactory.createInstance<T>()
