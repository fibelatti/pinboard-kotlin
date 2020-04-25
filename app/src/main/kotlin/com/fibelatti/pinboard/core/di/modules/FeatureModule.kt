package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.features.appstate.AppStateDataSource
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.notes.data.NotesApi
import com.fibelatti.pinboard.features.notes.data.NotesDataSource
import com.fibelatti.pinboard.features.notes.domain.NotesRepository
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.posts.data.PostsDataSource
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import com.fibelatti.pinboard.features.tags.data.TagsApi
import com.fibelatti.pinboard.features.tags.data.TagsDataSource
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class FeatureModule {

    companion object {

        @Provides
        fun postsApi(retrofit: Retrofit): PostsApi = retrofit.create()

        @Provides
        fun tagsApi(retrofit: Retrofit): TagsApi = retrofit.create()

        @Provides
        fun notesApi(retrofit: Retrofit): NotesApi = retrofit.create()
    }

    @Binds
    @IntoMap
    @FragmentKey(SplashFragment::class)
    abstract fun splashFragment(splashFragment: SplashFragment): Fragment

    // region State
    @Binds
    abstract fun appStateRepository(appStateDataSource: AppStateDataSource): AppStateRepository
    // endregion

    // region Posts
    @Binds
    abstract fun postsRepository(postsDataSource: PostsDataSource): PostsRepository

    @Binds
    @IntoMap
    @FragmentKey(PostListFragment::class)
    abstract fun postListFragment(postListFragment: PostListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostDetailFragment::class)
    abstract fun postDetailFragment(postDetailFragment: PostDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EditPostFragment::class)
    abstract fun editPostFragment(editPostFragment: EditPostFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostSearchFragment::class)
    abstract fun postSearchFragment(postSearchFragment: PostSearchFragment): Fragment
    // endregion

    // region Tags
    @Binds
    abstract fun tagsRepository(tagsDataSource: TagsDataSource): TagsRepository

    @Binds
    @IntoMap
    @FragmentKey(TagsFragment::class)
    abstract fun tagsFragment(tagsFragment: TagsFragment): Fragment
    // endregion

    // region Notes
    @Binds
    abstract fun notesRepository(notesDataSource: NotesDataSource): NotesRepository

    @Binds
    @IntoMap
    @FragmentKey(NoteListFragment::class)
    abstract fun noteListFragment(noteListFragment: NoteListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(NoteDetailsFragment::class)
    abstract fun noteDetailsFragment(noteDetailsFragment: NoteDetailsFragment): Fragment
    // endregion

    // region Popular
    @Binds
    @IntoMap
    @FragmentKey(PopularPostsFragment::class)
    abstract fun popularPostsFragment(popularPostsFragment: PopularPostsFragment): Fragment
    // endregion

    // region Preferences
    @Binds
    @IntoMap
    @FragmentKey(UserPreferencesFragment::class)
    abstract fun userPreferencesFragment(userPreferencesFragment: UserPreferencesFragment): Fragment
    // endregion
}
