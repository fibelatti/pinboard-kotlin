package com.fibelatti.pinboard.features

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.features.navigation.NavigationDrawerFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_splash.*

private const val SPLASH_DELAY = 500L

class MainActivity :
    BaseActivity(),
    NavigationDrawerFragment.Callback {

    private val mainMenuFabListener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)

            fab?.setImageResource(R.drawable.ic_add)
            fab?.show()

            bottomAppBar.run {
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(R.menu.menu_main)

                setOnMenuItemClickListener { item: MenuItem? ->
                    when (item?.itemId) {
                        R.id.menuItemSearch -> showLinkMenu()
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

            fab?.setImageResource(R.drawable.ic_share)
            fab?.show()

            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_link)

                setOnMenuItemClickListener { item: MenuItem? ->
                    when (item?.itemId) {
                        R.id.menuItemDelete -> {
                            showMainMenu()
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

    private val authViewModel: AuthViewModel by lazy { viewModelFactory.get<AuthViewModel>(this) }

    private var navigationFragment: NavigationDrawerFragment? = null

    private val handler by lazy { Handler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inTransaction {
            add(R.id.fragmentHost, SplashFragment.newInstance())
        }

        observe(authViewModel.loginState, ::handleLoginState)
        error(authViewModel.error, ::handleError)
    }

    override fun onAllClicked() {
        // TODO
    }

    override fun onRecentClicked() {
        // TODO
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
        navigationFragment?.dismiss()
    }

    override fun onPreferencesClicked() {
        // TODO
    }

    fun showMainMenu() {
        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
        fabMain.hide(mainMenuFabListener)
    }

    fun showLinkMenu() {
        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
        fabMain.hide(linkMenuFabListener)
    }

    private fun showNavigation() {
        NavigationDrawerFragment()
            .apply { callback = this@MainActivity }
            .also { navigationFragment = it }
            .run { show(supportFragmentManager, tag) }
    }

    private fun handleLoginState(loginState: LoginState) {
        when (loginState) {
            LoginState.LoggedIn -> {
                handler.postDelayed({
                    inTransaction {
                        replace(R.id.fragmentHost, PostListFragment.newInstance())
                            .addSharedElement(imageViewAppLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    bottomAppBar.visible()
                    fabMain.show()
                    showMainMenu()
                }, SPLASH_DELAY)
            }
            LoginState.LoggedOut -> {
                handler.postDelayed({
                    inTransaction {
                        replace(R.id.fragmentHost, AuthFragment.newInstance())
                            .addSharedElement(imageViewAppLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    bottomAppBar.gone()
                    fabMain.hide()
                }, SPLASH_DELAY)
            }
            LoginState.Unauthorized -> {
                inTransaction {
                    supportFragmentManager.fragments.forEach { remove(it) }
                    add(R.id.fragmentHost, AuthFragment.newInstance())
                }

                bottomAppBar.gone()
                fabMain.gone()

                Toast.makeText(this, R.string.auth_logged_out_feedback, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
