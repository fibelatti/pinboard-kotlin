package com.fibelatti.pinboard.features

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.isVisible
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

    private val navigationViewModel: NavigationViewModel by lazy { viewModelFactory.get<NavigationViewModel>(this) }
    private val authViewModel: AuthViewModel by lazy { viewModelFactory.get<AuthViewModel>(this) }

    private val handler by lazy { Handler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inTransaction {
            add(R.id.fragmentHost, createFragment<SplashFragment>())
        }

        setupView()
        setupViewModels()

        supportFragmentManager.run {
            addOnBackStackChangedListener { navigationViewModel.backNavigation(backStackEntryCount) }
        }
    }

    private fun setupView() {
        bottomAppBar?.setNavigationOnClickListener { showNavigation() }
    }

    private fun setupViewModels() {
        with(authViewModel) {
            observe(loginState, ::handleLoginState)
            error(error, ::handleError)
        }
    }

    inline fun updateTitleLayout(titleUpdates: TitleLayout.() -> Unit) {
        layoutTitle.run(titleUpdates)
    }

    inline fun updateViews(crossinline update: (BottomAppBar, FloatingActionButton) -> Unit = { _, _ -> }) {
        update(bottomAppBar, fabMain)
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
        when (loginState) {
            LoginState.LoggedIn -> {
                handler.postDelayed({
                    inTransaction {
                        replace(R.id.fragmentHost, createFragment<PostListFragment>())
                            .addSharedElement(authViewLogo, SharedElementTransitionNames.APP_LOGO)
                    }

                    showControls()
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

    fun snackbar(message: String) {
        layoutRoot.snackbar(message) {
            anchorView = if (fabMain.isVisible()) fabMain else bottomAppBar
        }
    }
}

val Fragment.mainActivity: MainActivity? get() = activity as? MainActivity
