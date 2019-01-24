package com.fibelatti.pinboard.features

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.core.app.ShareCompat
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.remove
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig.MARKET_BASE_URL
import com.fibelatti.pinboard.core.AppConfig.PLAY_STORE_BASE_URL
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.extension.createFragment
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.snackbar
import com.fibelatti.pinboard.features.navigation.NavigationDrawerFragment
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_auth.imageViewAppLogo as authViewLogo
import kotlinx.android.synthetic.main.fragment_splash.imageViewAppLogo as splashViewLogo

private const val SPLASH_DELAY = 500L

class MainActivity :
    BaseActivity(),
    NavigationDrawerFragment.Callback {

    private val mainMenuFabListener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)

            fab?.run {
                setOnClickListener { addLink() }
                setImageResource(R.drawable.ic_pin)
                show()
            }

            bottomAppBar?.run {
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(R.menu.menu_main)

                setOnMenuItemClickListener { item: MenuItem? ->
                    when (item?.itemId) {
                        R.id.menuItemSearch -> {
                        }
                        R.id.menuItemSort -> {
                            // postListFragment?.toggleSorting()
                        }
                    }

                    return@setOnMenuItemClickListener true
                }

                setNavigationOnClickListener { showNavigation() }
            }
        }
    }
    private val linkMenuFabListener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)

            fab?.run {
                setOnClickListener { shareLink() }
                setImageResource(R.drawable.ic_share)
                show()
            }

            bottomAppBar?.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_link)

                setOnMenuItemClickListener { item: MenuItem? ->
                    when (item?.itemId) {
                        R.id.menuItemDelete -> {
                        }
                        R.id.menuItemEditLink -> {
                        }
                        R.id.menuItemLinkTags -> {
                        }
                    }

                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private val navigationViewModel: NavigationViewModel by lazy { viewModelFactory.get<NavigationViewModel>(this) }
    private val authViewModel: AuthViewModel by lazy { viewModelFactory.get<AuthViewModel>(this) }

    private val handler by lazy { Handler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inTransaction {
            add(R.id.fragmentHost, createFragment<SplashFragment>())
        }

        setupViewModels()
    }

    private fun setupViewModels() {
        with(authViewModel) {
            observe(loginState, ::handleLoginState)
            error(error, ::handleError)
        }
        with(navigationViewModel) {
            observe(title, layoutTitle::setTitle)
            observe(postCount, layoutTitle::setPostCount)
            observe(menuType) {
                when (it) {
                    NavigationViewModel.MenuType.MAIN -> showMainMenu()
                    NavigationViewModel.MenuType.LINK -> showLinkMenu()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigationViewModel.viewList()
    }

    override fun onAllClicked() {
        navigationViewModel.viewContent(NavigationViewModel.ContentType.ALL)
    }

    override fun onRecentClicked() {
        navigationViewModel.viewContent(NavigationViewModel.ContentType.RECENT)
    }

    override fun onPublicClicked() {
        // TODO
    }

    override fun onPrivateClicked() {
        // TODO
    }

    override fun onUnreadClicked() {
        // TODO
    }

    override fun onUntaggedClicked() {
        // TODO
    }

    override fun onTagsClicked() {
        // TODO
    }

    override fun onLogoutClicked() {
        authViewModel.logout()
    }

    override fun onShareAppClicked() {
        val appName = packageName.remove(".debug")
        val message = getString(R.string.share_text, "$PLAY_STORE_BASE_URL$appName")

        ShareCompat.IntentBuilder.from(this)
            .setType("text/plain")
            .setChooserTitle(R.string.share_title)
            .setText(message)
            .startChooser()
    }

    override fun onRateAppClicked() {
        val appName = packageName.remove(".debug")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$MARKET_BASE_URL$appName")))
        } catch (exception: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$PLAY_STORE_BASE_URL$appName")))
        }
    }

    private fun showNavigation() {
        NavigationDrawerFragment().showNavigation(this)
    }

    private fun showMainMenu() {
        layoutTitle.hideNavigateUp()
        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
        bottomAppBar.show()
        fabMain.hide(mainMenuFabListener)
    }

    private fun showLinkMenu() {
        layoutTitle.setNavigateUp { onBackPressed() }
        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
        fabMain.hide(linkMenuFabListener)
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

    private fun handleLoginState(loginState: LoginState) {
        when (loginState) {
            LoginState.LoggedIn -> {
                handler.postDelayed({
                    inTransaction {
                        replace(R.id.fragmentHost, createFragment<PostListFragment>())
                            .addSharedElement(authViewLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    showControls()
                    showMainMenu()
                    layoutTitle.setTitle(R.string.posts_title_all)
                }, SPLASH_DELAY)
            }
            LoginState.LoggedOut -> {
                handler.postDelayed({
                    inTransaction {
                        setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        replace(R.id.fragmentHost, createFragment<AuthFragment>())
                            .addSharedElement(splashViewLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    hideControls()
                }, SPLASH_DELAY)
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

    private fun addLink() {
        // TODO
    }

    private fun shareLink() {
        navigationViewModel.post.value?.let {
            ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setChooserTitle(R.string.posts_share_title)
                .setText(it.url)
                .startChooser()
        }
    }
}
