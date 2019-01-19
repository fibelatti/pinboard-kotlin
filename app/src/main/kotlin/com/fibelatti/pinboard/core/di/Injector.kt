package com.fibelatti.pinboard.core.di

import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.features.user.presentation.AuthFragment

interface Injector {
    fun inject(baseActivity: BaseActivity)

    fun inject(authFragment: AuthFragment)
}
