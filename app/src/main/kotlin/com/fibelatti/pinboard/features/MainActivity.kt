package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import com.fibelatti.core.android.IntentDelegate
import com.fibelatti.core.android.base.BaseIntentBuilder
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig.MAIN_PACKAGE_NAME
import com.fibelatti.pinboard.core.AppConfig.PLAY_STORE_BASE_URL
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.customview.TitleLayout
import com.fibelatti.pinboard.core.extension.createFragment
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.core.extension.snackbar
import com.fibelatti.pinboard.core.functional.DoNothing
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.fibelatti.pinboard.features.navigation.NavigationDrawer
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsFragment
import com.fibelatti.pinboard.features.notes.presentation.NoteListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostAddFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_auth.imageViewAppLogo as authViewLogo
import kotlinx.android.synthetic.main.fragment_splash.imageViewAppLogo as splashViewLogo

val Fragment.mainActivity: MainActivity? get() = activity as? MainActivity
var Intent.fromBuilder by IntentDelegate.Boolean("FROM_BUILDER", false)

class MainActivity : BaseActivity() {

    private val appStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(this) }
    private val authViewModel by lazy { viewModelFactory.get<AuthViewModel>(this) }

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!intent.fromBuilder) {
            inTransaction {
                add(R.id.fragmentHost, createFragment<SplashFragment>())
            }
        }

        setupView()
        setupViewModels()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        appStateViewModel.runAction(NavigateBack)
    }

    private fun setupView() {
        bottomAppBar?.setNavigationOnClickListener {
            NavigationDrawer.show(this, NavigationCallback())
        }
    }

    private fun setupViewModels() {
        observe(authViewModel.loginState, ::handleLoginState)
        observe(authViewModel.error, ::handleError)
        observe(appStateViewModel.content) { content ->
            when (content) {
                is PostListContent -> showPostList()
                is PostDetailContent -> showPostDetail()
                is SearchContent -> showSearch()
                is AddPostContent -> showAddPost()
                is EditPostContent -> showEditPost()
                is TagListContent -> showTags()
                is NoteListContent -> showNotes()
                is NoteDetailContent -> showNoteDetail()
                is UserPreferencesContent -> showPreferences()
                is ExternalContent -> {
                    appStateViewModel.reset()
                    finish()
                }
            }.exhaustive
        }
    }

    // region Fragment transitions
    private fun popTo(tag: String) {
        for (fragment in supportFragmentManager.fragments.reversed()) {
            if (fragment.tag != tag) {
                supportFragmentManager.popBackStack()
            } else {
                break
            }
        }
    }

    private fun slideFromTheRight(fragment: Fragment, tag: String, addToBackStack: Boolean = true) {
        inTransaction {
            setCustomAnimations(
                R.anim.slide_right_in,
                R.anim.slide_left_out,
                R.anim.slide_left_in,
                R.anim.slide_right_out
            )
            add(R.id.fragmentHost, fragment, tag)

            if (addToBackStack) {
                addToBackStack(tag)
            }
        }
    }

    private fun slideUp(fragment: Fragment, tag: String, addToBackStack: Boolean = true) {
        inTransaction {
            setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
            add(R.id.fragmentHost, fragment, tag)

            if (addToBackStack) {
                addToBackStack(tag)
            }
        }
    }
    // endregion

    private fun showPostList() {
        popTo(PostListFragment.TAG)
    }

    private fun showPostDetail() {
        if (supportFragmentManager.findFragmentByTag(PostDetailFragment.TAG) == null) {
            slideFromTheRight(createFragment<PostDetailFragment>(), PostDetailFragment.TAG)
        } else {
            popTo(PostDetailFragment.TAG)
        }
    }

    private fun showSearch() {
        if (supportFragmentManager.findFragmentByTag(PostSearchFragment.TAG) == null) {
            slideUp(createFragment<PostSearchFragment>(), PostSearchFragment.TAG)
        }
    }

    private fun showAddPost() {
        if (supportFragmentManager.findFragmentByTag(PostAddFragment.TAG) == null) {
            slideUp(createFragment<PostAddFragment>(), PostAddFragment.TAG)
        }
    }

    private fun showTags() {
        if (supportFragmentManager.findFragmentByTag(TagsFragment.TAG) == null) {
            slideUp(createFragment<TagsFragment>(), TagsFragment.TAG)
        }
    }

    private fun showNotes() {
        if (supportFragmentManager.findFragmentByTag(NoteListFragment.TAG) == null) {
            slideFromTheRight(createFragment<NoteListFragment>(), NoteListFragment.TAG)
        } else {
            popTo(NoteListFragment.TAG)
        }
    }

    private fun showNoteDetail() {
        if (supportFragmentManager.findFragmentByTag(NoteDetailsFragment.TAG) == null) {
            slideFromTheRight(createFragment<NoteDetailsFragment>(), NoteDetailsFragment.TAG)
        }
    }

    private fun showPreferences() {
        if (supportFragmentManager.findFragmentByTag(UserPreferencesFragment.TAG) == null) {
            slideFromTheRight(
                createFragment<UserPreferencesFragment>(),
                UserPreferencesFragment.TAG
            )
        }
    }

    private fun showEditPost() {
        if (supportFragmentManager.findFragmentByTag(PostAddFragment.TAG) == null) {
            slideUp(
                createFragment<PostAddFragment>(),
                PostAddFragment.TAG,
                addToBackStack = !intent.fromBuilder
            )
        }
    }

    fun updateTitleLayout(titleUpdates: TitleLayout.() -> Unit) {
        layoutTitle.visible()
        layoutTitle.run(titleUpdates)
    }

    fun updateViews(update: (BottomAppBar, FloatingActionButton) -> Unit = { _, _ -> }) {
        update(bottomAppBar, fabMain)
    }

    private fun handleLoginState(loginState: LoginState) {
        val animTime = resources.getInteger(R.integer.anim_time_long).toLong()

        when (loginState) {
            LoginState.Authorizing -> DoNothing
            LoginState.LoggedIn -> {
                if (intent.fromBuilder) {
                    return
                }

                val runnable = {
                    inTransaction {
                        replace(
                            R.id.fragmentHost,
                            createFragment<PostListFragment>(),
                            PostListFragment.TAG
                        )
                        authViewLogo?.let {
                            addSharedElement(it, SharedElementTransitionNames.APP_LOGO)
                        }
                    }
                }

                handler.postDelayed(runnable, animTime)
            }
            LoginState.LoggedOut -> {
                val runnable = {
                    inTransaction {
                        setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        replace(R.id.fragmentHost, createFragment<AuthFragment>())
                        splashViewLogo?.let {
                            addSharedElement(it, SharedElementTransitionNames.APP_LOGO)
                        }
                    }

                    hideControls()
                }

                handler.postDelayed(runnable, animTime)
            }
            LoginState.Unauthorized -> {
                inTransaction {
                    for (fragment in supportFragmentManager.fragments) {
                        remove(fragment)
                    }
                    setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    add(R.id.fragmentHost, createFragment<AuthFragment>())
                }

                hideControls()

                layoutRoot.snackbar(getString(R.string.auth_logged_out_feedback))
            }
        }
    }

    private fun hideControls() {
        layoutTitle.gone()
        bottomAppBar.gone()
        fabMain.hide()
    }

    inner class NavigationCallback : NavigationDrawer.Callback {
        override fun onAllClicked() {
            appStateViewModel.runAction(All)
        }

        override fun onRecentClicked() {
            appStateViewModel.runAction(Recent)
        }

        override fun onPublicClicked() {
            appStateViewModel.runAction(Public)
        }

        override fun onPrivateClicked() {
            appStateViewModel.runAction(Private)
        }

        override fun onUnreadClicked() {
            appStateViewModel.runAction(Unread)
        }

        override fun onUntaggedClicked() {
            appStateViewModel.runAction(Untagged)
        }

        override fun onTagsClicked() {
            appStateViewModel.runAction(ViewTags)
        }

        override fun onNotesClicked() {
            appStateViewModel.runAction(ViewNotes)
        }

        override fun onPreferencesClicked() {
            appStateViewModel.runAction(ViewPreferences)
        }

        override fun onLogoutClicked() {
            authViewModel.logout()
        }

        override fun onShareAppClicked() {
            shareText(
                R.string.share_title,
                getString(R.string.share_text, "$PLAY_STORE_BASE_URL$MAIN_PACKAGE_NAME")
            )
        }

        override fun onRateAppClicked() {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("$PLAY_STORE_BASE_URL$MAIN_PACKAGE_NAME")
                    setPackage("com.android.vending")
                }
            )
        }
    }

    class Builder(context: Context) : BaseIntentBuilder(context, MainActivity::class.java) {

        init {
            intent.fromBuilder = true
        }
    }
}
