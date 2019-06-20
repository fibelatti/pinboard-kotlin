package com.fibelatti.pinboard.core.di

import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.android.base.BaseActivity

interface Injector {
    fun inject(application: App)

    fun inject(baseActivity: BaseActivity)
}
