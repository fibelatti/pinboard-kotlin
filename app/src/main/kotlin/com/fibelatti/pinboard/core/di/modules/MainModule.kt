package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.core.di.mapkeys.MainReducerKey
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.SavedFiltersContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.main.reducer.BookmarkDetailsReducer
import com.fibelatti.pinboard.features.main.reducer.BookmarkEditorReducer
import com.fibelatti.pinboard.features.main.reducer.BookmarkListReducer
import com.fibelatti.pinboard.features.main.reducer.MainStateReducer
import com.fibelatti.pinboard.features.main.reducer.NoteDetailsReducer
import com.fibelatti.pinboard.features.main.reducer.NoteListReducer
import com.fibelatti.pinboard.features.main.reducer.PopularBookmarksReducer
import com.fibelatti.pinboard.features.main.reducer.SavedFiltersReducer
import com.fibelatti.pinboard.features.main.reducer.SearchReducer
import com.fibelatti.pinboard.features.main.reducer.TagListReducer
import com.fibelatti.pinboard.features.main.reducer.UserPreferencesReducer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {

    @Binds
    @IntoMap
    @MainReducerKey(PostListContent::class)
    abstract fun bookmarkListReducer(impl: BookmarkListReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(PostDetailContent::class)
    abstract fun bookmarkDetailsReducer(impl: BookmarkDetailsReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(EditPostContent::class)
    abstract fun bookmarkEditorReducer(impl: BookmarkEditorReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(SearchContent::class)
    abstract fun searchReducer(impl: SearchReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(SavedFiltersContent::class)
    abstract fun savedFiltersReducer(impl: SavedFiltersReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(PopularPostsContent::class)
    abstract fun popularBookmarksReducer(impl: PopularBookmarksReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(PopularPostDetailContent::class)
    abstract fun popularBookmarkDetailsReducer(impl: BookmarkDetailsReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(TagListContent::class)
    abstract fun tagListReducer(impl: TagListReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(NoteListContent::class)
    abstract fun noteListReducer(impl: NoteListReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(NoteDetailContent::class)
    abstract fun noteDetailsReducer(impl: NoteDetailsReducer): MainStateReducer

    @Binds
    @IntoMap
    @MainReducerKey(UserPreferencesContent::class)
    abstract fun userPreferencesReducer(impl: UserPreferencesReducer): MainStateReducer
}
