package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import com.fibelatti.core.android.extension.getSharedPreferences
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.features.ContainerFragment
import com.fibelatti.pinboard.features.filters.data.SavedFiltersDataSource
import com.fibelatti.pinboard.features.filters.domain.SavedFiltersRepository
import com.fibelatti.pinboard.features.filters.presentation.SavedFiltersFragment
import com.fibelatti.pinboard.features.notes.data.NotesDataSource
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.data.PostsDataSourceProxy
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.tags.data.TagsDataSourceProxy
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.data.UserDataSource
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FeatureModule {

    @Binds
    @IntoMap
    @FragmentKey(ContainerFragment::class)
    abstract fun containerFragment(fragment: ContainerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AuthFragment::class)
    abstract fun authFragment(fragment: AuthFragment): Fragment

    // region Posts
    @Binds
    @IntoMap
    @FragmentKey(PostListFragment::class)
    abstract fun postListFragment(fragment: PostListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostDetailFragment::class)
    abstract fun postDetailFragment(fragment: PostDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EditPostFragment::class)
    abstract fun editPostFragment(fragment: EditPostFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostSearchFragment::class)
    abstract fun postSearchFragment(fragment: PostSearchFragment): Fragment

    @Binds
    abstract fun postsRepository(impl: PostsDataSourceProxy): PostsRepository
    // endregion

    // region Tags
    @Binds
    @IntoMap
    @FragmentKey(TagsFragment::class)
    abstract fun tagsFragment(fragment: TagsFragment): Fragment

    @Binds
    abstract fun tagsRepository(impl: TagsDataSourceProxy): TagsRepository
    // endregion

    // region Saved filters
    @Binds
    @IntoMap
    @FragmentKey(SavedFiltersFragment::class)
    abstract fun savedFiltersFragment(fragment: SavedFiltersFragment): Fragment

    @Binds
    abstract fun savedFiltersRepository(impl: SavedFiltersDataSource): SavedFiltersRepository
    // endregion Saved filters

    // region Notes
    @Binds
    @IntoMap
    @FragmentKey(NoteListFragment::class)
    abstract fun noteListFragment(fragment: NoteListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(NoteDetailsFragment::class)
    abstract fun noteDetailsFragment(fragment: NoteDetailsFragment): Fragment

    @Binds
    abstract fun notesRepository(impl: NotesDataSource): NotesRepository
    // endregion

    // region Popular
    @Binds
    @IntoMap
    @FragmentKey(PopularPostsFragment::class)
    abstract fun popularPostFragment(fragment: PopularPostsFragment): Fragment
    // endregion

    // region Preferences
    @Binds
    @IntoMap
    @FragmentKey(UserPreferencesFragment::class)
    abstract fun userPreferencesFragment(fragment: UserPreferencesFragment): Fragment

    @Binds
    abstract fun userRepository(impl: UserDataSource): UserRepository
    // endregion

    companion object {

        @Provides
        fun sharedPreferences(
            application: Application,
        ): SharedPreferences = application.getSharedPreferences("user_preferences")
    }
}
