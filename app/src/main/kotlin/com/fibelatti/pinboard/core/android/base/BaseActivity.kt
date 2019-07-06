package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import com.fibelatti.core.di.ViewModelFactory
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.toast
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    private val injector
        get() = (application as App).appComponent

    @Inject
    lateinit var fragmentFactory: FragmentFactory
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)
    }

    fun handleError(error: Throwable) {
        toast(getString(R.string.generic_msg_error))
        if (BuildConfig.DEBUG) error.printStackTrace()
    }
}
