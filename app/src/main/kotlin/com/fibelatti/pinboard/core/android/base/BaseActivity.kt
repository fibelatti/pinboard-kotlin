package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.fibelatti.pinboard.core.di.modules.ActivityEntryPoint
import dagger.hilt.android.EntryPointAccessors

abstract class BaseActivity : AppCompatActivity() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint = EntryPointAccessors.fromActivity(this, ActivityEntryPoint::class.java)
        supportFragmentManager.fragmentFactory = entryPoint.getFragmentFactory()

        super.onCreate(savedInstanceState)
    }
}
