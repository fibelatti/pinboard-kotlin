package com.fibelatti.pinboard.features

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.fibelatti.core.extension.createFragment
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.popTo
import com.fibelatti.pinboard.core.extension.slideFromTheRight
import com.fibelatti.pinboard.core.extension.slideUp
import com.fibelatti.pinboard.features.MainActivity.Companion.fromBuilder
import com.fibelatti.pinboard.features.filters.presentation.SavedFiltersFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import javax.inject.Inject

class FeatureFragments @Inject constructor(private val activity: FragmentActivity) {

    fun showLogin() {
        with(activity.supportFragmentManager) {
            if (findFragmentByTag(AuthFragment.TAG) == null) {
                commit {
                    setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    for (fragment in fragments) {
                        remove(fragment)
                    }
                    add(R.id.fragment_host, activity.createFragment<AuthFragment>(), AuthFragment.TAG)
                }
            }
        }
    }

    fun showPostList() {
        if (activity.supportFragmentManager.findFragmentByTag(PostListFragment.TAG) == null) {
            activity.supportFragmentManager.commit {
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                replace(R.id.fragment_host, activity.createFragment<PostListFragment>(), PostListFragment.TAG)
            }
        } else {
            activity.popTo(PostListFragment.TAG)
        }
    }

    fun showPostDetail(postId: String) {
        val tag = "${PostDetailFragment.TAG}_$postId"

        activity.supportFragmentManager.commit {
            replace(R.id.fragment_host_side_panel, activity.createFragment<PostDetailFragment>(), tag)
            addToBackStack(tag)
        }
    }

    fun showPostInExternalBrowser(post: Post) {
        activity.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(post.url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    fun showSearch() {
        if (activity.supportFragmentManager.findFragmentByTag(PostSearchFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<PostSearchFragment>(),
                tag = PostSearchFragment.TAG,
            )
        }
    }

    fun showAddPost() {
        if (activity.supportFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<EditPostFragment>(),
                tag = EditPostFragment.TAG,
            )
        }
    }

    fun showTags() {
        if (activity.supportFragmentManager.findFragmentByTag(TagsFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<TagsFragment>(),
                tag = TagsFragment.TAG,
            )
        }
    }

    fun showSavedFilters() {
        if (activity.supportFragmentManager.findFragmentByTag(SavedFiltersFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<SavedFiltersFragment>(),
                tag = SavedFiltersFragment.TAG,
            )
        }
    }

    fun showNotes() {
        if (activity.supportFragmentManager.findFragmentByTag(NoteListFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<NoteListFragment>(),
                tag = NoteListFragment.TAG,
            )
        } else {
            activity.popTo(NoteListFragment.TAG)
        }
    }

    fun showNoteDetails() {
        if (activity.supportFragmentManager.findFragmentByTag(NoteDetailsFragment.TAG) == null) {
            activity.slideFromTheRight(
                containerId = R.id.fragment_host_side_panel,
                fragment = activity.createFragment<NoteDetailsFragment>(),
                tag = NoteDetailsFragment.TAG,
            )
        }
    }

    fun showPopular() {
        if (activity.supportFragmentManager.findFragmentByTag(PopularPostsFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<PopularPostsFragment>(),
                tag = PopularPostsFragment.TAG,
            )
        } else {
            activity.popTo(PopularPostsFragment.TAG)
        }
    }

    fun showPreferences() {
        if (activity.supportFragmentManager.findFragmentByTag(UserPreferencesFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<UserPreferencesFragment>(),
                tag = UserPreferencesFragment.TAG,
            )
        }
    }

    fun showEditPost() {
        if (activity.supportFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            activity.slideUp(
                containerId = R.id.fragment_host,
                fragment = activity.createFragment<EditPostFragment>(),
                tag = EditPostFragment.TAG,
                addToBackStack = !activity.intent.fromBuilder,
            )
        }
    }
}
