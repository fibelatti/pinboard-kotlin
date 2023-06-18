package com.fibelatti.pinboard.features

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fibelatti.core.android.BaseIntentBuilder
import com.fibelatti.core.android.intentExtras
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
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val appStateViewModel: AppStateViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var featureFragments: FeatureFragments

    @Inject
    lateinit var inAppUpdateManager: InAppUpdateManager

    private var isRecreating: Boolean = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            appStateViewModel.runAction(NavigateBack)
        }
    }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isRecreating = savedInstanceState != null

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setupView()
        setupAccessibility()
        setupViewModel()
        setupAutoUpdate()
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
                featureFragments.multiPanelEnabled = windowSizeClass != WindowSizeClass.COMPACT
                mainViewModel.updateState { currentState ->
                    currentState.copy(multiPanelEnabled = windowSizeClass != WindowSizeClass.COMPACT)
                }
            },
        )

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

    private fun setupAutoUpdate() {
        if (!userRepository.autoUpdate) return

        var autoUpdateJob: Job? = null
        autoUpdateJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (inAppUpdateManager.isUpdateAvailable()) {
                    inAppUpdateManager.downloadUpdate(fragmentActivity = this@MainActivity)

                    Snackbar.make(binding.root, R.string.in_app_update_ready, Snackbar.LENGTH_LONG)
                        .apply { setAction(R.string.in_app_update_install) { inAppUpdateManager.installUpdate() } }
                        .show()
                } else {
                    autoUpdateJob?.cancel()
                }
            }
        }
    }

    private fun handleContent(content: Content) {
        onBackPressedCallback.isEnabled = (content as? ContentWithHistory)?.previousContent !is ExternalContent

        if (isRecreating) {
            isRecreating = false
            return
        }

        if (supportFragmentManager.isStateSaved) {
            return
        }

        setupSidePanel(show = content is SidePanelContent)
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
            is PostDetailContent -> featureFragments.showPostDetail(content.post.hash)
            is ExternalBrowserContent -> {
                featureFragments.showPostInExternalBrowser(content.post)
                appStateViewModel.runAction(NavigateBack)
            }

            is SearchContent -> featureFragments.showSearch()
            is AddPostContent -> featureFragments.showAddPost()
            is EditPostContent -> featureFragments.showEditPost()
            is TagListContent -> featureFragments.showTags()
            is NoteListContent -> featureFragments.showNotes()
            is NoteDetailContent -> featureFragments.showNoteDetails()
            is PopularPostsContent -> featureFragments.showPopular()
            is PopularPostDetailContent -> featureFragments.showPostDetail(content.post.hash)
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

        val showSidePanel = featureFragments.multiPanelEnabled && show

        when {
            showSidePanel && binding.fragmentHostSidePanel.isGone -> {
                binding.fragmentHostSidePanel.isVisible = true
                animatedSidePanelWidth(endWidth = binding.root.width / 2)
            }

            !showSidePanel && binding.fragmentHostSidePanel.isVisible -> {
                animatedSidePanelWidth(endWidth = 0) {
                    binding.fragmentHostSidePanel.isVisible = false
                }
            }
        }
    }

    private inline fun animatedSidePanelWidth(
        endWidth: Int,
        crossinline doOnEnd: () -> Unit = {},
    ) {
        ValueAnimator.ofInt(binding.fragmentHostSidePanel.measuredWidth, endWidth)
            .apply {
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    binding.fragmentHostSidePanel.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        width = value
                    }
                }
                doOnEnd { doOnEnd() }
            }
            .start()
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
