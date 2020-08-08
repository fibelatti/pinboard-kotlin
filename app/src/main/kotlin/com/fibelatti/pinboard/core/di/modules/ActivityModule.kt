package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.android.MultiBindingFragmentFactory
import com.fibelatti.core.provider.ResourceProvider
import dagger.Binds
import dagger.Module

@Module
abstract class ActivityModule {

    @Binds
    abstract fun FragmentActivity.context(): Context

    @Binds
    abstract fun MultiBindingFragmentFactory.fragmentFactory(): FragmentFactory

    @Binds
    abstract fun AppResourceProvider.resourceProvider(): ResourceProvider
}
