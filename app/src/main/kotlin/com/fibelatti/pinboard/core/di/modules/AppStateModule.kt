package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.mapkeys.ActionHandlerKey
import com.fibelatti.pinboard.features.appstate.ActionHandler
import com.fibelatti.pinboard.features.appstate.NavigationAction
import com.fibelatti.pinboard.features.appstate.NavigationActionHandler
import com.fibelatti.pinboard.features.appstate.NoteAction
import com.fibelatti.pinboard.features.appstate.NoteActionHandler
import com.fibelatti.pinboard.features.appstate.PopularAction
import com.fibelatti.pinboard.features.appstate.PopularActionHandler
import com.fibelatti.pinboard.features.appstate.PostAction
import com.fibelatti.pinboard.features.appstate.PostActionHandler
import com.fibelatti.pinboard.features.appstate.SearchAction
import com.fibelatti.pinboard.features.appstate.SearchActionHandler
import com.fibelatti.pinboard.features.appstate.TagAction
import com.fibelatti.pinboard.features.appstate.TagActionHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class AppStateModule {

    @Binds
    @IntoMap
    @ActionHandlerKey(NavigationAction::class)
    abstract fun navigationActionHandler(impl: NavigationActionHandler): ActionHandler<*>

    @Binds
    @IntoMap
    @ActionHandlerKey(PostAction::class)
    abstract fun postActionHandler(impl: PostActionHandler): ActionHandler<*>

    @Binds
    @IntoMap
    @ActionHandlerKey(SearchAction::class)
    abstract fun searchActionHandler(impl: SearchActionHandler): ActionHandler<*>

    @Binds
    @IntoMap
    @ActionHandlerKey(TagAction::class)
    abstract fun tagActionHandler(impl: TagActionHandler): ActionHandler<*>

    @Binds
    @IntoMap
    @ActionHandlerKey(NoteAction::class)
    abstract fun noteActionHandler(impl: NoteActionHandler): ActionHandler<*>

    @Binds
    @IntoMap
    @ActionHandlerKey(PopularAction::class)
    abstract fun popularActionHandler(impl: PopularActionHandler): ActionHandler<*>
}
