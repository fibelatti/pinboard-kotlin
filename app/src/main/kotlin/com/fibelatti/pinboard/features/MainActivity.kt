package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.android.IntentDelegate
import com.fibelatti.core.android.base.BaseIntentBuilder
import com.fibelatti.core.extension.applyAs
import com.fibelatti.core.extension.createFragment
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.exhaustive
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.inTransaction
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.DefaultAnimationListener
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.android.customview.TitleLayout
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.core.functional.DoNothing
import com.fibelatti.pinboard.databinding.ActivityMainBinding
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
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
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

val Fragment.mainActivity: MainActivity? get() = activity as? MainActivity
var Intent.fromBuilder by IntentDelegate.Boolean("FROM_BUILDER", false)

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    companion object {

        private const val FLEXIBLE_UPDATE_REQUEST = 1001
    }

    private val appStateViewModel: AppStateViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var featureFragments: FeatureFragments

    @Inject
    lateinit var inAppUpdateManager: InAppUpdateManager

    private var isRecreating: Boolean = false

    private val binding by viewBinding(ActivityMainBinding::inflate)

    // An action that will run once when the Activity is resumed and will be set to null afterwards
    private var onResumeDelegate: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isRecreating = savedInstanceState != null

        setupBackNavigation()
        setupView()
        setupViewModels()

        inAppUpdateManager.checkForAvailableUpdates(this, FLEXIBLE_UPDATE_REQUEST, ::onUpdateDownloadComplete)
    }

    override fun onResume() {
        super.onResume()
        onResumeDelegate?.invoke()
        onResumeDelegate = null
    }

    private fun onUpdateDownloadComplete() {
        Snackbar.make(binding.root, getString(R.string.in_app_update_ready), Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.in_app_update_install) { inAppUpdateManager.completeUpdate() }
            show()
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
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.layoutTitle.doOnApplyWindowInsets { view, insets, initialPadding, _ ->
            view.updatePadding(top = initialPadding.top + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
        }

        binding.fabMain.doOnApplyWindowInsets { view, insets, _, _ ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_small) +
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            }

            // Remove once we're done to prevent the Fab from appearing over the keyboard
            view.doOnApplyWindowInsets { _, _, _, _ -> }
        }

        binding.bottomAppBar.doOnApplyWindowInsets { view, insets, padding, _ ->
            view.updatePadding(bottom = padding.bottom + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)

            // Remove once we're done to prevent the BottomAppBar from appearing over the keyboard
            view.doOnApplyWindowInsets { _, _, _, _ -> }
        }

        binding.bottomAppBar.setNavigationOnClickListener {
            createFragment<NavigationMenuFragment>().applyAs<Fragment, BottomSheetDialogFragment> {
                show(supportFragmentManager, NavigationMenuFragment.TAG)
            }
        }
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            authViewModel.loginState.collect(::handleLoginState)
        }
        lifecycleScope.launch {
            authViewModel.error.collect(::handleError)
        }
        lifecycleScope.launch {
            appStateViewModel.content.collect { content ->
                if (isRecreating) {
                    isRecreating = false
                    return@collect
                }

                if (supportFragmentManager.isStateSaved) {
                    return@collect
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
                        appStateViewModel.reset()
                        finish()
                    }
                }.exhaustive
            }
        }
    }

    fun updateTitleLayout(titleUpdates: TitleLayout.() -> Unit) {
        binding.layoutTitle.visible()
        binding.layoutTitle.run(titleUpdates)
    }

    fun updateViews(update: (BottomAppBar, FloatingActionButton) -> Unit = { _, _ -> }) {
        update(binding.bottomAppBar, binding.fabMain)
    }

    private fun handleLoginState(loginState: LoginState) {
        if (isRecreating) {
            return
        }

        when (loginState) {
            LoginState.Authorizing -> DoNothing
            LoginState.LoggedIn -> handleLoggedIn()
            LoginState.LoggedOut -> handleLoggedOut()
            LoginState.Unauthorized -> handleUnauthorized()
        }
    }

    private fun handleLoggedIn() {
        if (intent.fromBuilder) {
            return
        }

        val animTime = resources.getInteger(R.integer.anim_time_long).toLong()
        lifecycleScope.launch {
            delay(animTime)
            inTransaction {
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                replace(R.id.fragmentHost, createFragment<PostListFragment>(), PostListFragment.TAG)
            }
        }
    }

    private fun handleLoggedOut() {
        val animTime = resources.getInteger(R.integer.anim_time_long).toLong()
        lifecycleScope.launch {
            delay(animTime)
            inTransaction {
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                replace(R.id.fragmentHost, createFragment<AuthFragment>())
            }

            hideControls()
        }
    }

    private fun handleUnauthorized() {
        inTransaction {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            for (fragment in supportFragmentManager.fragments) {
                remove(fragment)
            }
            add(R.id.fragmentHost, createFragment<AuthFragment>(), AuthFragment.TAG)
        }

        hideControls()

        showBanner(message = getString(R.string.auth_logged_out_feedback))
    }

    private fun hideControls() {
        binding.layoutTitle.gone()
        binding.bottomAppBar.gone()
        binding.fabMain.hide()
    }

    override fun handleError(error: Throwable) {
        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        if (error.isServerException()) {
            showBanner(getString(R.string.server_timeout_error))
        } else {
            sendErrorReport(error)
        }
    }

    @Suppress("MagicNumber")
    fun showBanner(message: String) {
        val banner = layoutInflater.inflate(R.layout.layout_feedback_banner, binding.layoutContent, false)
            .apply { findViewById<TextView>(R.id.textViewFeedback).text = message }
            .also(binding.layoutContent::addView)

        ConstraintSet().apply {
            clone(binding.layoutContent)
            connect(banner.id, ConstraintSet.TOP, binding.layoutTitle.id, ConstraintSet.BOTTOM, 0)
            connect(banner.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            connect(banner.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        }.applyTo(binding.layoutContent)

        val animTime = resources.getInteger(R.integer.anim_time_long).toLong()
        val disappearAnimation = AlphaAnimation(1F, 0F).apply {
            duration = animTime
            startOffset = animTime * 5
            setAnimationListener(object : DefaultAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    binding.layoutContent.removeView(banner)
                }
            })
        }

        val appearAnimation = AlphaAnimation(0F, 1F).apply {
            duration = animTime
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
