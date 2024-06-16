package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Scope(AppDispatchers.IO)
    fun ioScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Provides
    @Scope(AppDispatchers.DEFAULT)
    fun defaultScope(): CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Provides
    fun sharingStarted(): SharingStarted = SharingStarted.Eagerly
}
