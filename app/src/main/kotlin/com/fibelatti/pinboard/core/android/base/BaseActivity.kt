package com.fibelatti.pinboard.core.android.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import com.fibelatti.core.di.ViewModelFactory
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    val injector get() = (application as App).appComponent

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var userSharedPreferences: UserSharedPreferences

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
    }

    fun handleError(error: Throwable) {
        error.printStackTrace()
    }
}
