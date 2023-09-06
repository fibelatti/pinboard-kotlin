package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(ActivityComponent::class)
abstract class FeatureModule {

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
    // endregion

    // region Tags
    @Binds
    @IntoMap
    @FragmentKey(TagsFragment::class)
    abstract fun tagsFragment(fragment: TagsFragment): Fragment
    // endregion

    // region Notes
    @Binds
    @IntoMap
    @FragmentKey(NoteListFragment::class)
    abstract fun noteListFragment(fragment: NoteListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(NoteDetailsFragment::class)
    abstract fun noteDetailsFragment(fragment: NoteDetailsFragment): Fragment
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
    // endregion
}
