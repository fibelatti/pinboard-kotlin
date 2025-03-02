package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Scope(AppDispatchers.IO)
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Scope(AppDispatchers.DEFAULT)
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Scope(AppDispatchers.IO)
    fun ioScope(
        @Scope(AppDispatchers.IO) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(dispatcher + SupervisorJob())

    @Provides
    @Scope(AppDispatchers.DEFAULT)
    fun defaultScope(
        @Scope(AppDispatchers.DEFAULT) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(dispatcher + SupervisorJob())

    @Provides
    fun sharingStarted(): SharingStarted = SharingStarted.Eagerly
}
