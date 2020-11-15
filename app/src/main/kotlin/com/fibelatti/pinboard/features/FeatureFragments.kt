package com.fibelatti.pinboard.features

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.fibelatti.core.extension.createFragment
import com.fibelatti.pinboard.core.extension.popTo
import com.fibelatti.pinboard.core.extension.slideFromTheRight
import com.fibelatti.pinboard.core.extension.slideUp
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import javax.inject.Inject

class FeatureFragments @Inject constructor(private val activity: FragmentActivity) {

    fun showPostList() {
        if (activity.supportFragmentManager.findFragmentByTag(PostListFragment.TAG) == null) {
            activity.slideUp(activity.createFragment<PostListFragment>(), PostListFragment.TAG)
        } else {
            activity.popTo(PostListFragment.TAG)
        }
    }

    fun showPostDetail() {
        if (activity.supportFragmentManager.findFragmentByTag(PostDetailFragment.TAG) == null) {
            activity.slideFromTheRight(activity.createFragment<PostDetailFragment>(), PostDetailFragment.TAG)
        } else {
            activity.popTo(PostDetailFragment.TAG)
        }
    }

    fun showPostInExternalBrowser(post: Post) {
        activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(post.url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun showSearch() {
        if (activity.supportFragmentManager.findFragmentByTag(PostSearchFragment.TAG) == null) {
            activity.slideUp(activity.createFragment<PostSearchFragment>(), PostSearchFragment.TAG)
        }
    }

    fun showAddPost() {
        if (activity.supportFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            activity.slideUp(activity.createFragment<EditPostFragment>(), EditPostFragment.TAG)
        }
    }

    fun showTags() {
        if (activity.supportFragmentManager.findFragmentByTag(TagsFragment.TAG) == null) {
            activity.slideUp(activity.createFragment<TagsFragment>(), TagsFragment.TAG)
        }
    }

    fun showNotes() {
        if (activity.supportFragmentManager.findFragmentByTag(NoteListFragment.TAG) == null) {
            activity.slideFromTheRight(activity.createFragment<NoteListFragment>(), NoteListFragment.TAG)
        } else {
            activity.popTo(NoteListFragment.TAG)
        }
    }

    fun showNoteDetails() {
        if (activity.supportFragmentManager.findFragmentByTag(NoteDetailsFragment.TAG) == null) {
            activity.slideFromTheRight(activity.createFragment<NoteDetailsFragment>(), NoteDetailsFragment.TAG)
        }
    }

    fun showPopular() {
        if (activity.supportFragmentManager.findFragmentByTag(PopularPostsFragment.TAG) == null) {
            activity.slideFromTheRight(activity.createFragment<PopularPostsFragment>(), PopularPostsFragment.TAG)
        } else {
            activity.popTo(PopularPostsFragment.TAG)
        }
    }

    fun showPreferences() {
        if (activity.supportFragmentManager.findFragmentByTag(UserPreferencesFragment.TAG) == null) {
            activity.slideFromTheRight(activity.createFragment<UserPreferencesFragment>(), UserPreferencesFragment.TAG)
        }
    }

    fun showEditPost() {
        if (activity.supportFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            activity.slideUp(
                activity.createFragment<EditPostFragment>(),
                EditPostFragment.TAG,
                addToBackStack = !activity.intent.fromBuilder
            )
        }
    }
}
