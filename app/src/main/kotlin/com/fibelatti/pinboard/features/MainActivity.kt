package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.fibelatti.core.android.BaseIntentBuilder
import com.fibelatti.core.android.intentExtras
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.doOnInitializeAccessibilityNodeInfo
import com.fibelatti.core.extension.setupForAccessibility
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.WindowSizeClass
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.android.widthWindowSizeClassReactiveView
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.databinding.ActivityMainBinding
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.ContentWithHistory
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.ExternalBrowserContent
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.SavedFiltersContent
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val appStateViewModel: AppStateViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var featureFragments: FeatureFragments

    private var isRecreating: Boolean = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            appStateViewModel.runAction(NavigateBack)
        }
    }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    // region ConstraintSets
    private val constraintSetSidePanelHidden by lazy {
        ConstraintSet().apply {
            clone(binding.layoutContent)

            connect(R.id.fragment_host, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(R.id.fragment_host, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constrainPercentWidth(R.id.fragment_host, 1f)

            connect(R.id.fragment_host_side_panel, ConstraintSet.START, R.id.fragment_host, ConstraintSet.END)
            connect(R.id.fragment_host_side_panel, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            setVisibility(R.id.fragment_host_side_panel, View.GONE)
        }
    }

    private val constraintSetSidePanelOverlap by lazy {
        ConstraintSet().apply {
            clone(binding.layoutContent)

            connect(R.id.fragment_host, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(R.id.fragment_host, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constrainPercentWidth(R.id.fragment_host, 1f)

            connect(R.id.fragment_host_side_panel, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(R.id.fragment_host_side_panel, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            setVisibility(R.id.fragment_host_side_panel, View.VISIBLE)
        }
    }

    private val constraintSetSidePanelDivided by lazy {
        ConstraintSet().apply {
            clone(binding.layoutContent)

            connect(R.id.fragment_host, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(R.id.fragment_host, ConstraintSet.END, R.id.fragment_host_side_panel, ConstraintSet.START)
            constrainPercentWidth(R.id.fragment_host, 0.45f)

            connect(R.id.fragment_host_side_panel, ConstraintSet.START, R.id.fragment_host, ConstraintSet.END)
            connect(R.id.fragment_host_side_panel, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            setVisibility(R.id.fragment_host_side_panel, View.VISIBLE)
        }
    }
    // endregion ConstraintSets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isRecreating = savedInstanceState != null

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setupView()
        setupAccessibility()
        setupViewModel()
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            appStateViewModel.reset()
        }

        super.onDestroy()
    }

    private fun setupView() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.root.addView(
            widthWindowSizeClassReactiveView { windowSizeClass ->
                mainViewModel.updateState { currentState ->
                    currentState.copy(multiPanelEnabled = windowSizeClass != WindowSizeClass.COMPACT)
                }
            },
        )

        binding.layoutContent.animateChangingTransitions()

        binding.composeViewTopBar.setThemedContent {
            MainTopAppBar(
                mainViewModel = mainViewModel,
                appStateViewModel = appStateViewModel,
            )
        }

        binding.composeViewBottomBar.setThemedContent {
            MainBottomAppBar(
                mainViewModel = mainViewModel,
                appStateViewModel = appStateViewModel,
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentHostSidePanel) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(right = navigationBars.right)
            insets
        }
    }

    private fun setupAccessibility() {
        supportFragmentManager.setupForAccessibility()

        binding.composeViewBottomBar.doOnInitializeAccessibilityNodeInfo { info ->
            info.setTraversalAfter(binding.composeViewTopBar)
        }
        binding.fragmentHost.doOnInitializeAccessibilityNodeInfo { info ->
            info.setTraversalAfter(binding.composeViewBottomBar)
        }
    }

    private fun setupViewModel() {
        appStateViewModel.content
            .onEach(::handleContent)
            .launchInAndFlowWith(this, minActiveState = Lifecycle.State.RESUMED)
    }

    private fun handleContent(content: Content) {
        onBackPressedCallback.isEnabled = (content as? ContentWithHistory)?.previousContent !is ExternalContent

        setupSidePanel(show = content is SidePanelContent)

        if (isRecreating) {
            isRecreating = false
            return
        }

        if (supportFragmentManager.isStateSaved) {
            return
        }

        showContentScreen(content)
    }

    private fun showContentScreen(content: Content) {
        when (content) {
            is LoginContent -> {
                if (content.isUnauthorized) {
                    binding.root.showBanner(message = getString(R.string.auth_logged_out_feedback))
                }
                featureFragments.showLogin()
            }

            is PostListContent -> featureFragments.showPostList()
            is PostDetailContent -> featureFragments.showPostDetail(content.post.id)
            is ExternalBrowserContent -> {
                featureFragments.showPostInExternalBrowser(content.post)
                appStateViewModel.runAction(NavigateBack)
            }

            is SearchContent -> featureFragments.showSearch()
            is AddPostContent -> featureFragments.showAddPost()
            is EditPostContent -> featureFragments.showEditPost()
            is TagListContent -> featureFragments.showTags()
            is SavedFiltersContent -> featureFragments.showSavedFilters()
            is NoteListContent -> featureFragments.showNotes()
            is NoteDetailContent -> featureFragments.showNoteDetails()
            is PopularPostsContent -> featureFragments.showPopular()
            is PopularPostDetailContent -> featureFragments.showPostDetail(content.post.id)
            is UserPreferencesContent -> featureFragments.showPreferences()
            is ExternalContent -> {
                appStateViewModel.reset()
                finish()
            }
        }
    }

    private fun setupSidePanel(show: Boolean) {
        mainViewModel.updateState { currentState ->
            currentState.copy(multiPanelContent = show)
        }

        val constraintSet = when {
            show && mainViewModel.state.value.multiPanelEnabled -> constraintSetSidePanelDivided
            show -> constraintSetSidePanelOverlap
            else -> constraintSetSidePanelHidden.also {
                supportFragmentManager.findFragmentById(R.id.fragment_host_side_panel)?.let { fragment ->
                    supportFragmentManager.commit { remove(fragment) }
                }
            }
        }

        val transition = ChangeBounds()
            .setInterpolator(LinearInterpolator())
            .setDuration(300)

        TransitionManager.beginDelayedTransition(binding.layoutContent, transition)
        constraintSet.applyTo(binding.layoutContent)
    }

    override fun handleError(error: Throwable?, postAction: () -> Unit) {
        error ?: return

        if (BuildConfig.DEBUG) {
            error.printStackTrace()
        }

        if (error.isServerException()) {
            binding.root.showBanner(getString(R.string.server_timeout_error))
            postAction()
        } else {
            sendErrorReport(error, postAction = postAction)
        }
    }

    class Builder(context: Context) : BaseIntentBuilder(context, MainActivity::class.java) {

        init {
            intent.fromBuilder = true
        }
    }

    companion object {

        var Intent.fromBuilder by intentExtras(false)
    }
}
