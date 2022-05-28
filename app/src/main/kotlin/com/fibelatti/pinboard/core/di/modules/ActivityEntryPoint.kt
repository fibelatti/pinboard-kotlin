package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.MultiBindingFragmentFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ActivityEntryPoint {

    fun getFragmentFactory(): MultiBindingFragmentFactory
}
