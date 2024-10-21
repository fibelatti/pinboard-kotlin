package com.fibelatti.pinboard.features

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.fibelatti.core.android.extension.createFragment
import com.fibelatti.core.android.extension.doOnApplyWindowInsets
import com.fibelatti.core.android.platform.fragmentArgs
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.popTo
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class FeatureFragments @Inject constructor(private val activity: FragmentActivity) {

    private val mainPanelFragment: Fragment
        get() = requireNotNull(activity.supportFragmentManager.findFragmentById(R.id.fragment_host))
    private val sidePanelFragment: Fragment
        get() = requireNotNull(activity.supportFragmentManager.findFragmentById(R.id.fragment_host_side_panel))

    private fun Fragment.requireViewId(): Int = requireView().id

    fun setup() {
        activity.supportFragmentManager.commitNow {
            replace(
                R.id.fragment_host,
                activity.createFragment<ContainerFragment>().apply {
                    applyNavBarInsets = false
                },
            )
            replace(
                R.id.fragment_host_side_panel,
                activity.createFragment<ContainerFragment>().apply {
                    applyNavBarInsets = true
                },
            )
        }
    }

    fun showLogin() {
        with(mainPanelFragment.childFragmentManager) {
            if (findFragmentByTag(AuthFragment.TAG) == null) {
                commit {
                    for (fragment in fragments) remove(fragment)
                    add(mainPanelFragment.requireViewId(), activity.createFragment<AuthFragment>(), AuthFragment.TAG)
                }
            }
        }
    }

    fun showPostList() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(PostListFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.commit {
                replace(
                    mainPanelFragment.requireViewId(),
                    activity.createFragment<PostListFragment>(),
                    PostListFragment.TAG,
                )
            }
        } else {
            mainPanelFragment.childFragmentManager.popTo(PostListFragment.TAG)
        }

        sidePanelFragment.childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun showPostDetail(postId: String) {
        val tag = "${PostDetailFragment.TAG}_$postId"
        if (sidePanelFragment.childFragmentManager.findFragmentByTag(tag) == null) {
            sidePanelFragment.childFragmentManager.commit {
                replace(sidePanelFragment.requireViewId(), activity.createFragment<PostDetailFragment>(), tag)
                addToBackStack(tag)
            }
        } else {
            sidePanelFragment.childFragmentManager.popTo(tag)

            if (mainPanelFragment.childFragmentManager.findFragmentByTag(PopularPostsFragment.TAG) != null) {
                mainPanelFragment.childFragmentManager.popTo(PopularPostsFragment.TAG)
            } else {
                mainPanelFragment.childFragmentManager.popTo(PostListFragment.TAG)
            }
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
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(PostSearchFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<PostSearchFragment>(),
                tag = PostSearchFragment.TAG,
            )
        }
    }

    fun showAddPost() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<EditPostFragment>(),
                tag = EditPostFragment.TAG,
            )
        }
    }

    fun showTags() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(TagsFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<TagsFragment>(),
                tag = TagsFragment.TAG,
            )
        }
    }

    fun showSavedFilters() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(SavedFiltersFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<SavedFiltersFragment>(),
                tag = SavedFiltersFragment.TAG,
            )
        }
    }

    fun showNotes() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(NoteListFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<NoteListFragment>().apply {
                    enterTransitionRes = R.transition.slide_up
                },
                tag = NoteListFragment.TAG,
            )
        } else {
            mainPanelFragment.childFragmentManager.popTo(NoteListFragment.TAG)
        }

        sidePanelFragment.childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun showNoteDetails() {
        sidePanelFragment.childFragmentManager.commit {
            replace(
                sidePanelFragment.requireViewId(),
                activity.createFragment<NoteDetailsFragment>(),
                NoteDetailsFragment.TAG,
            )
            addToBackStack(NoteDetailsFragment.TAG)
        }
    }

    fun showPopular() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(PopularPostsFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<PopularPostsFragment>(),
                tag = PopularPostsFragment.TAG,
            )
        } else {
            mainPanelFragment.childFragmentManager.popTo(PopularPostsFragment.TAG)
        }

        sidePanelFragment.childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun showPreferences() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(UserPreferencesFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<UserPreferencesFragment>(),
                tag = UserPreferencesFragment.TAG,
            )
        }
    }

    fun showEditPost() {
        if (mainPanelFragment.childFragmentManager.findFragmentByTag(EditPostFragment.TAG) == null) {
            mainPanelFragment.childFragmentManager.slideUp(
                containerId = mainPanelFragment.requireViewId(),
                fragment = activity.createFragment<EditPostFragment>(),
                tag = EditPostFragment.TAG,
                addToBackStack = !activity.intent.fromBuilder,
            )
        }
    }
}

@AndroidEntryPoint
class ContainerFragment @Inject constructor() : Fragment(R.layout.fragment_container) {

    var applyNavBarInsets: Boolean? by fragmentArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (applyNavBarInsets == true) {
            view.doOnApplyWindowInsets { _, windowInsets, _, _ ->
                val insets = WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout()
                view.updatePadding(right = windowInsets.getInsets(insets).right)
            }
        }
    }
}
