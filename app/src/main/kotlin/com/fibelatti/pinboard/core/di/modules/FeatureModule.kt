package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.features.navigation.NavigationMenuFragment
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
import dagger.multibindings.IntoMap

@Module
abstract class FeatureModule {

    @Binds
    @IntoMap
    @FragmentKey(AuthFragment::class)
    abstract fun AuthFragment.authFragment(): Fragment

    // region Posts
    @Binds
    @IntoMap
    @FragmentKey(PostListFragment::class)
    abstract fun PostListFragment.postListFragment(): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostDetailFragment::class)
    abstract fun PostDetailFragment.postDetailFragment(): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EditPostFragment::class)
    abstract fun EditPostFragment.editPostFragment(): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostSearchFragment::class)
    abstract fun PostSearchFragment.postSearchFragment(): Fragment
    // endregion

    // region Tags
    @Binds
    @IntoMap
    @FragmentKey(TagsFragment::class)
    abstract fun TagsFragment.tagsFragment(): Fragment
    // endregion

    // region Notes
    @Binds
    @IntoMap
    @FragmentKey(NoteListFragment::class)
    abstract fun NoteListFragment.noteListFragment(): Fragment

    @Binds
    @IntoMap
    @FragmentKey(NoteDetailsFragment::class)
    abstract fun NoteDetailsFragment.fragment(): Fragment
    // endregion

    // region Popular
    @Binds
    @IntoMap
    @FragmentKey(PopularPostsFragment::class)
    abstract fun PopularPostsFragment.popularPostFragment(): Fragment
    // endregion

    // region Preferences
    @Binds
    @IntoMap
    @FragmentKey(UserPreferencesFragment::class)
    abstract fun UserPreferencesFragment.userPreferencesFragment(): Fragment
    // endregion

    // region Menu
    @Binds
    @IntoMap
    @FragmentKey(NavigationMenuFragment::class)
    abstract fun NavigationMenuFragment.navigationMenuFragment(): Fragment
    // endregion
}
