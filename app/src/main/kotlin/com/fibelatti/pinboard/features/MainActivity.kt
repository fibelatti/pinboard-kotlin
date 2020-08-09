package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.fibelatti.core.android.IntentDelegate
import com.fibelatti.core.android.base.BaseIntentBuilder
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.createFragment
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.snackbar
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.DefaultAnimationListener
import com.fibelatti.pinboard.core.android.SharedElementTransitionNames
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.customview.TitleLayout
import com.fibelatti.pinboard.core.functional.DoNothing
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.ExternalBrowserContent
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.navigation.NavigationMenuFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_auth.imageViewAppLogo as authViewLogo
import kotlinx.android.synthetic.main.fragment_splash.imageViewAppLogo as splashViewLogo

val Fragment.mainActivity: MainActivity? get() = activity as? MainActivity
var Intent.fromBuilder by IntentDelegate.Boolean("FROM_BUILDER", false)

class MainActivity : BaseActivity(R.layout.activity_main) {

    companion object {

        private const val FLEXIBLE_UPDATE_REQUEST = 1001
    }

    private val appStateViewModel by viewModel { viewModelProvider.appStateViewModel() }
    private val authViewModel by viewModel { viewModelProvider.authViewModel() }

    private val featureFragments get() = activityComponent.featureFragments()
    private val inAppUpdateManager get() = activityComponent.inAppUpdateManager()

    private val handler = Handler()

    private var isRecreating: Boolean = false

    // An action that will run once when the Activity is resumed and will be set to null afterwards
    private var onResumeDelegate: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRecreating = savedInstanceState != null

        if (!intent.fromBuilder && !isRecreating) {
            inTransaction {
                add(R.id.fragmentHost, createFragment<SplashFragment>())
            }
        }

        setupBackNavigation()
        setupView()
        setupViewModels()

        inAppUpdateManager.checkForAvailableUpdates(
            this,
            FLEXIBLE_UPDATE_REQUEST,
            ::onUpdateDownloadComplete
        )
    }

    override fun onResume() {
        super.onResume()
        onResumeDelegate?.invoke()
        onResumeDelegate = null
    }

    private fun onUpdateDownloadComplete() {
        layoutRoot.snackbar(
            message = getString(R.string.in_app_update_ready),
            textColor = R.color.text_primary,
            marginSize = R.dimen.margin_regular,
            background = R.drawable.background_snackbar,
            duration = Snackbar.LENGTH_LONG
        ) {
            setAction(R.string.in_app_update_install) { inAppUpdateManager.completeUpdate() }
            setActionTextColor(
                ContextCompat.getColor(
                    layoutRoot.context,
                    R.color.color_on_background
                )
            )
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                appStateViewModel.runAction(NavigateBack)
            }
        })
    }

    private fun setupView() {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        layoutTitle?.doOnApplyWindowInsets { view, insets, _, initialMargin ->
            view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                leftMargin = initialMargin.left
                topMargin = initialMargin.top + insets.systemWindowInsetTop
                rightMargin = initialMargin.right
                bottomMargin = initialMargin.bottom
            }
        }

        fabMain?.doOnApplyWindowInsets { view, insets, _, initialMargin ->
            view.layoutParams = (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                leftMargin = initialMargin.left
                topMargin = initialMargin.top
                rightMargin = initialMargin.right
                bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_small) +
                    insets.systemWindowInsetBottom
            }

            // Remove once we're done to prevent the Fab from appearing over the keyboard
            view.doOnApplyWindowInsets { _, _, _, _ -> }
        }

        bottomAppBar?.doOnApplyWindowInsets { view, insets, padding, _ ->
            ViewCompat.setPaddingRelative(
                view,
                padding.start,
                padding.top,
                padding.end,
                padding.bottom + insets.systemWindowInsetBottom
            )

            // Remove once we're done to prevent the BottomAppBar from appearing over the keyboard
            view.doOnApplyWindowInsets { _, _, _, _ -> }
        }

        bottomAppBar?.setNavigationOnClickListener {
            createFragment<NavigationMenuFragment>().applyAs<Fragment, BottomSheetDialogFragment> {
                show(supportFragmentManager, NavigationMenuFragment.TAG)
            }
        }
    }

    private fun setupViewModels() {
        observe(authViewModel.loginState, ::handleLoginState)
        observe(authViewModel.error, ::handleError)
        observe(appStateViewModel.content) { content ->
            if (isRecreating) {
                isRecreating = false
                return@observe
            }

            when (content) {
                is PostListContent -> featureFragments.showPostList()
                is PostDetailContent -> featureFragments.showPostDetail()
                is ExternalBrowserContent -> {
                    featureFragments.showPostInExternalBrowser(content.post)
                    onResumeDelegate = { appStateViewModel.runAction(NavigateBack) }
                }
                is SearchContent -> featureFragments.showSearch()
                is AddPostContent -> featureFragments.showAddPost()
                is EditPostContent -> featureFragments.showEditPost()
                is TagListContent -> featureFragments.showTags()
                is NoteListContent -> featureFragments.showNotes()
                is NoteDetailContent -> featureFragments.showNoteDetails()
                is PopularPostsContent -> featureFragments.showPopular()
                is PopularPostDetailContent -> featureFragments.showPostDetail()
                is UserPreferencesContent -> featureFragments.showPreferences()
                is ExternalContent -> {
                    appStateViewModel.content.removeObservers(this)
                    appStateViewModel.reset()
                    finish()
                }
            }.exhaustive
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
        if (isRecreating) {
            return
        }

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

                layoutRoot.snackbar(
                    message = getString(R.string.auth_logged_out_feedback),
                    textColor = R.color.text_primary,
                    marginSize = R.dimen.margin_regular,
                    background = R.drawable.background_snackbar
                )
            }
        }
    }

    private fun hideControls() {
        layoutTitle.gone()
        bottomAppBar.gone()
        fabMain.hide()
    }

    @Suppress("MagicNumber")
    fun showBanner(message: String) {
        val banner = layoutInflater.inflate(R.layout.layout_feedback_banner, null)
            .apply { findViewById<TextView>(R.id.textViewFeedback).text = message }
            .also(layoutContent::addView)

        ConstraintSet()
            .apply {
                clone(layoutContent)
                connect(banner.id, ConstraintSet.TOP, layoutTitle.id, ConstraintSet.BOTTOM, 16)
                connect(
                    banner.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START,
                    16
                )
                connect(
                    banner.id,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    16
                )
            }
            .applyTo(layoutContent)

        val disappearAnimation = AlphaAnimation(1F, 0F).apply {
            duration = 500L
            startOffset = 2_500L
            setAnimationListener(object : DefaultAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    layoutContent?.removeView(banner)
                }
            })
        }

        val appearAnimation = AlphaAnimation(0F, 1F).apply {
            duration = 500L
            setAnimationListener(object : DefaultAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    banner?.startAnimation(disappearAnimation)
                }
            })
        }

        banner.startAnimation(appearAnimation)
    }

    class Builder(context: Context) : BaseIntentBuilder(context, MainActivity::class.java) {

        init {
            intent.fromBuilder = true
        }
    }
}
