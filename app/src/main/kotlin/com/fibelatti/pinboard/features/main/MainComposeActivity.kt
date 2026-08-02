package com.fibelatti.pinboard.features.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.android.platform.BaseIntentBuilder
import com.fibelatti.core.android.platform.intentExtras
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.LocalNetworkAccessProvider
import com.fibelatti.pinboard.core.android.composable.AppMessageQueue
import com.fibelatti.pinboard.core.android.composable.LocalAppMessages
import com.fibelatti.pinboard.core.extension.canRequestPermissionAgain
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showLocalNetworkAccessDialog
import com.fibelatti.pinboard.core.extension.showLocalNetworkAccessSettingsDialog
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.notifications.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val appMessages: AppMessageQueue = AppMessageQueue(context = this, scope = lifecycleScope)

    @Inject
    lateinit var unauthorizedPluginProvider: UnauthorizedPluginProvider

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    private val localNetworkPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted: Boolean ->
        when {
            // No action required, the app self-recovers
            granted -> Unit

            canRequestPermissionAgain(LocalNetworkAccessProvider.PERMISSION) -> {
                appMessages.show(messageRes = R.string.auth_linkding_missing_local_network_permission)
            }

            else -> {
                showLocalNetworkAccessSettingsDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setThemedContent {
            CompositionLocalProvider(LocalAppMessages provides appMessages) {
                MainScreen()
            }
        }

        observeUnauthorized()
        observeLocalNetworkPermissionRequest()
        checkForDeeplink()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkForDeeplink()
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            mainViewModel.resetAppNavigation()
        }

        super.onDestroy()
    }

    private fun observeUnauthorized() {
        unauthorizedPluginProvider.unauthorized
            .onEach { appMessages.show(messageRes = R.string.auth_logged_out_feedback) }
            .flowWithLifecycle(lifecycle = lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)
    }

    private fun observeLocalNetworkPermissionRequest() {
        mainViewModel.localNetworkPermissionRequired
            .filter { required: Boolean -> required }
            .onEach {
                mainViewModel.localNetworkPermissionHandled()
                showLocalNetworkAccessDialog {
                    localNetworkPermissionLauncher.launch(LocalNetworkAccessProvider.PERMISSION)
                }
            }
            .flowWithLifecycle(lifecycle = lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)
    }

    private fun checkForDeeplink() {
        val notificationId: Int = intent.deeplinkNotificationId ?: return

        appNotificationManager.cancelNotification(notificationId)

        val postId: String? = intent.deeplinkPostId
        val openEditor: Boolean? = intent.deeplinkOpenEditor

        if (postId != null && openEditor != null) {
            mainViewModel.handleDeeplink(postId, openEditor)
            appMessages.show(messageRes = R.string.share_notification_opening_deep_link)
        }
    }

    class Builder(context: Context) : BaseIntentBuilder(context, MainComposeActivity::class.java) {

        fun notificationExtras(
            notificationId: Int,
            postId: String,
            openEditor: Boolean,
        ): Builder = apply {
            intent.deeplinkNotificationId = notificationId
            intent.deeplinkPostId = postId
            intent.deeplinkOpenEditor = openEditor
        }
    }

    private companion object {

        var Intent.deeplinkPostId: String? by intentExtras()
        var Intent.deeplinkNotificationId: Int? by intentExtras()
        var Intent.deeplinkOpenEditor: Boolean? by intentExtras()
    }
}
