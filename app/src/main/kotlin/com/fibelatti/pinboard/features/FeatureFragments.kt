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

    var multiPanelEnabled: Boolean = false

    private fun getDetailContainerId(): Int = if (multiPanelEnabled) {
        R.id.fragment_host_side_panel
    } else {
        R.id.fragment_host
    }

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

        if (activity.supportFragmentManager.findFragmentByTag(tag) == null) {
            activity.slideFromTheRight(
                fragment = activity.createFragment<PostDetailFragment>(),
                tag = tag,
                containerId = getDetailContainerId(),
            )
        } else {
            activity.popTo(tag)
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
            activity.slideUp(activity.createFragment<NoteListFragment>(), NoteListFragment.TAG)
        } else {
            activity.popTo(NoteListFragment.TAG)
        }
    }

    fun showNoteDetails() {
        if (activity.supportFragmentManager.findFragmentByTag(NoteDetailsFragment.TAG) == null) {
            activity.slideFromTheRight(
                fragment = activity.createFragment<NoteDetailsFragment>(),
                tag = NoteDetailsFragment.TAG,
                containerId = getDetailContainerId(),
            )
        }
    }

    fun showPopular() {
        if (activity.supportFragmentManager.findFragmentByTag(PopularPostsFragment.TAG) == null) {
            activity.slideUp(activity.createFragment<PopularPostsFragment>(), PopularPostsFragment.TAG)
        } else {
            activity.popTo(PopularPostsFragment.TAG)
        }
    }

    fun showPreferences() {
        if (activity.supportFragmentManager.findFragmentByTag(UserPreferencesFragment.TAG) == null) {
            activity.slideUp(activity.createFragment<UserPreferencesFragment>(), UserPreferencesFragment.TAG)
        }
    }

    fun showEditPost() {
        if (activity.supportFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            activity.slideUp(
                fragment = activity.createFragment<EditPostFragment>(),
                tag = EditPostFragment.TAG,
                addToBackStack = !activity.intent.fromBuilder,
            )
        }
    }
}
