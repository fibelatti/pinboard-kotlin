package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fibelatti.core.android.BaseIntentBuilder
import com.fibelatti.core.android.intentExtras
import com.fibelatti.core.extension.animateChangingTransitions
import com.fibelatti.core.extension.createFragment
import com.fibelatti.core.extension.doOnApplyWindowInsets
import com.fibelatti.core.extension.doOnInitializeAccessibilityNodeInfo
import com.fibelatti.core.extension.setupForAccessibility
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.base.sendErrorReport
import com.fibelatti.pinboard.core.android.composable.MainTitle
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.core.extension.launchInAndFlowWith
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.databinding.ActivityMainBinding
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ConnectionAwareContent
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
import com.fibelatti.pinboard.features.appstate.Refresh
import com.fibelatti.pinboard.features.appstate.RefreshPopular
import com.fibelatti.pinboard.features.appstate.SearchContent
import com.fibelatti.pinboard.features.appstate.TagListContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.navigation.NavigationMenuFragment
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

    // An action that will run once when the Activity is resumed and will be set to null afterwards
    private var onResumeDelegate: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isRecreating = savedInstanceState != null

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setupView()
        setupAccessibility()
        setupViewModels()
        setupAutoUpdate()
    }

    override fun onResume() {
        super.onResume()
        onResumeDelegate?.invoke()
        onResumeDelegate = null
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            appStateViewModel.reset()
        }

        super.onDestroy()
    }

    private fun setupView() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.composeViewTitle.doOnApplyWindowInsets { view, insets, initialPadding, _ ->
            view.updatePadding(top = initialPadding.top + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
        }

        binding.composeViewTitle.setThemedContent {
            MainTitle()
        }

        binding.layoutContent.animateChangingTransitions()

        binding.fabMain.doOnApplyWindowInsets { view, insets, _, _ ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = 8.dp.value.toInt() + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
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
            createFragment<NavigationMenuFragment>().show(supportFragmentManager, NavigationMenuFragment.TAG)
        }
    }

    private fun setupAccessibility() {
        supportFragmentManager.setupForAccessibility()

        binding.bottomAppBar.doOnInitializeAccessibilityNodeInfo { info ->
            info.setTraversalAfter(binding.composeViewTitle)
        }
        binding.fabMain.doOnInitializeAccessibilityNodeInfo { info ->
            info.setTraversalAfter(binding.bottomAppBar)
        }
        binding.fragmentHost.doOnInitializeAccessibilityNodeInfo { info ->
            info.setTraversalAfter(binding.fabMain)
        }
    }

    private fun setupViewModels() {
        appStateViewModel.content
            .onEach(::handleContent)
            .launchInAndFlowWith(this)

        mainViewModel.state
            .onEach { state ->
                updateBottomAppBarComponent(state)
                updateFabComponent(state)
            }
            .launchInAndFlowWith(this)
    }

    private fun updateBottomAppBarComponent(state: MainState) {
        when (state.bottomAppBar) {
            is MainState.BottomAppBarComponent.Gone -> {
                binding.bottomAppBar.navigationIcon = null
                binding.bottomAppBar.menu.clear()
                binding.bottomAppBar.isGone = true
            }
            is MainState.BottomAppBarComponent.Visible -> {
                val icon = state.bottomAppBar.navigationIcon
                if (icon != null) {
                    binding.bottomAppBar.setNavigationIcon(icon)
                } else {
                    binding.bottomAppBar.navigationIcon = null
                }

                binding.bottomAppBar.replaceMenu(state.bottomAppBar.menu)
                binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
                    mainViewModel.menuItemClicked(
                        id = state.bottomAppBar.id,
                        menuItemId = menuItem.itemId,
                        data = state.bottomAppBar.data,
                    )
                    true
                }

                binding.bottomAppBar.isVisible = true
                binding.bottomAppBar.show()
            }
        }
    }

    private fun updateFabComponent(state: MainState) {
        when (state.floatingActionButton) {
            is MainState.FabComponent.Gone -> binding.fabMain.hide()
            is MainState.FabComponent.Visible -> {
                binding.fabMain.setImageResource(state.floatingActionButton.icon)
                binding.fabMain.setOnClickListener {
                    mainViewModel.fabClicked(
                        id = state.floatingActionButton.id,
                        data = state.floatingActionButton.data,
                    )
                }
                binding.fabMain.show()
            }
        }
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

        binding.composeViewTitle.isGone = content is LoginContent

        if (isRecreating) {
            isRecreating = false
            return
        }

        if (supportFragmentManager.isStateSaved) {
            return
        }

        showContentScreen(content)
        handleConnectivity(content)
    }

    private fun handleConnectivity(content: Content) {
        binding.layoutOfflineAlert.isVisible = content is ConnectionAwareContent && !content.isConnected
        binding.buttonRetryConnection.isVisible = content is PostListContent || content is PopularPostsContent
        binding.buttonRetryConnection.setOnClickListener {
            val action = when (content) {
                is PostListContent -> Refresh()
                is PopularPostsContent -> RefreshPopular
                else -> null
            }

            action?.let(appStateViewModel::runAction)
        }
    }

    private fun showContentScreen(content: Content) {
        when (content) {
            is LoginContent -> {
                hideControls()
                if (content.isUnauthorized) {
                    binding.root.showBanner(message = getString(R.string.auth_logged_out_feedback))
                }
                featureFragments.showLogin()
            }

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
        }
    }

    private fun hideControls() {
        binding.bottomAppBar.isGone = true
        binding.fabMain.hide()
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
