package com.fibelatti.pinboard.features

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.remove
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig.PLAY_STORE_BASE_URL
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.customview.TitleLayout
import com.fibelatti.pinboard.core.extension.createFragment
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.core.extension.snackbar
import com.fibelatti.pinboard.core.functional.DoNothing
import com.fibelatti.pinboard.features.appstate.AddPostView
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.PostDetail
import com.fibelatti.pinboard.features.appstate.PostList
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SearchView
import com.fibelatti.pinboard.features.appstate.TagList
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.fibelatti.pinboard.features.navigation.NavigationDrawerFragment
import com.fibelatti.pinboard.features.posts.presentation.PostAddFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import com.fibelatti.pinboard.features.tags.presentation.TagsFragment
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_auth.imageViewAppLogo as authViewLogo
import kotlinx.android.synthetic.main.fragment_splash.imageViewAppLogo as splashViewLogo

val Fragment.mainActivity: MainActivity? get() = activity as? MainActivity

class MainActivity :
    BaseActivity(),
    NavigationDrawerFragment.Callback {

    private val appStateViewModel: AppStateViewModel by lazy { viewModelFactory.get<AppStateViewModel>(this) }
    private val authViewModel: AuthViewModel by lazy { viewModelFactory.get<AuthViewModel>(this) }

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inTransaction {
            add(R.id.fragmentHost, createFragment<SplashFragment>())
        }

        setupView()
        setupViewModels()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        appStateViewModel.runAction(NavigateBack)
    }

    private fun setupView() {
        bottomAppBar?.setNavigationOnClickListener { showNavigation() }
    }

    private fun setupViewModels() {
        with(authViewModel) {
            observe(loginState, ::handleLoginState)
            error(error, ::handleError)
        }

        observe(appStateViewModel.getContent()) { content ->
            when (content) {
                is PostList -> showPostList()
                is PostDetail -> showPostDetail()
                is SearchView -> showSearchView()
                is AddPostView -> showAddPostView()
                is TagList -> showTagsView()
            }.exhaustive
        }
    }

    private fun showPostList() {
        for (fragment in supportFragmentManager.fragments.reversed()) {
            if (fragment.tag != PostListFragment.TAG) {
                supportFragmentManager.popBackStack()
            } else {
                break
            }
        }
    }

    private fun showPostDetail() {
        if (supportFragmentManager.findFragmentByTag(PostDetailFragment.TAG) == null) {
            inTransaction {
                setCustomAnimations(
                    R.anim.slide_right_in,
                    R.anim.slide_left_out,
                    R.anim.slide_left_in,
                    R.anim.slide_right_out
                )
                add(R.id.fragmentHost, createFragment<PostDetailFragment>(), PostDetailFragment.TAG)
                addToBackStack(PostDetailFragment.TAG)
            }
        }
    }

    private fun showSearchView() {
        if (supportFragmentManager.findFragmentByTag(PostSearchFragment.TAG) == null) {
            inTransaction {
                setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
                add(R.id.fragmentHost, createFragment<PostSearchFragment>(), PostSearchFragment.TAG)
                addToBackStack(PostSearchFragment.TAG)
            }
        }
    }

    private fun showAddPostView() {
        if (supportFragmentManager.findFragmentByTag(PostAddFragment.TAG) == null) {
            inTransaction {
                setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
                add(R.id.fragmentHost, createFragment<PostAddFragment>(), PostAddFragment.TAG)
                addToBackStack(PostAddFragment.TAG)
            }
        }
    }

    private fun showTagsView() {
        if (supportFragmentManager.findFragmentByTag(TagsFragment.TAG) == null) {
            inTransaction {
                setCustomAnimations(R.anim.slide_up, -1, -1, R.anim.slide_down)
                add(R.id.fragmentHost, createFragment<TagsFragment>(), TagsFragment.TAG)
                addToBackStack(PostAddFragment.TAG)
            }
        }
    }

    inline fun updateTitleLayout(titleUpdates: TitleLayout.() -> Unit) {
        layoutTitle.run(titleUpdates)
    }

    inline fun updateViews(
        crossinline update: (BottomAppBar, FloatingActionButton) -> Unit = { _, _ -> }
    ) {
        update(bottomAppBar, fabMain)
    }

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

    override fun onLogoutClicked() {
        authViewModel.logout()
    }

    override fun onShareAppClicked() {
        shareText(
            R.string.share_title,
            getString(R.string.share_text, "$PLAY_STORE_BASE_URL${packageName.remove(".debug")}")
        )
    }

    override fun onRateAppClicked() {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("$PLAY_STORE_BASE_URL$${packageName.remove(".debug")}")
                setPackage("com.android.vending")
            }
        )
    }

    private fun showNavigation() {
        NavigationDrawerFragment().showNavigation(this)
    }

    private fun handleLoginState(loginState: LoginState) {
        val animTime = resources.getInteger(R.integer.anim_time_long).toLong()

        when (loginState) {
            LoginState.Authorizing -> DoNothing
            LoginState.LoggedIn -> {
                handler.postDelayed({
                    inTransaction {
                        replace(R.id.fragmentHost, createFragment<PostListFragment>(), PostListFragment.TAG)
                        addSharedElement(authViewLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    showControls()
                }, animTime)
            }
            LoginState.LoggedOut -> {
                handler.postDelayed({
                    inTransaction {
                        setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        replace(R.id.fragmentHost, createFragment<AuthFragment>())
                        addSharedElement(splashViewLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    hideControls()
                }, animTime)
            }
            LoginState.Unauthorized -> {
                inTransaction {
                    supportFragmentManager.fragments.forEach { remove(it) }
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

    private fun showControls() {
        layoutTitle.visible()
        bottomAppBar.visible()
        fabMain.show()
    }
}
