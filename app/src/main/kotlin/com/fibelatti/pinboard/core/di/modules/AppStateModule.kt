package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.features.appstate.ActionHandler
import com.fibelatti.pinboard.features.appstate.AppStateDataSource
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.NavigationActionHandler
import com.fibelatti.pinboard.features.appstate.NoteActionHandler
import com.fibelatti.pinboard.features.appstate.PopularActionHandler
import com.fibelatti.pinboard.features.appstate.PostActionHandler
import com.fibelatti.pinboard.features.appstate.SearchActionHandler
import com.fibelatti.pinboard.features.appstate.TagActionHandler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AppStateModule {

    @Binds
    @IntoSet
    abstract fun navigationActionHandler(impl: NavigationActionHandler): ActionHandler<*>

    @Binds
    @IntoSet
    abstract fun postActionHandler(impl: PostActionHandler): ActionHandler<*>

    @Binds
    @IntoSet
    abstract fun searchActionHandler(impl: SearchActionHandler): ActionHandler<*>

    @Binds
    @IntoSet
    abstract fun tagActionHandler(impl: TagActionHandler): ActionHandler<*>

    @Binds
    @IntoSet
    abstract fun noteActionHandler(impl: NoteActionHandler): ActionHandler<*>

    @Binds
    @IntoSet
    abstract fun popularActionHandler(impl: PopularActionHandler): ActionHandler<*>

    @Binds
    abstract fun appStateRepository(impl: AppStateDataSource): AppStateRepository

    companion object {

        @Provides
        fun actionHandlers(actionHandlers: Set<@JvmSuppressWildcards ActionHandler<*>>): List<ActionHandler<*>> {
            return actionHandlers.toList()
        }
    }
}
