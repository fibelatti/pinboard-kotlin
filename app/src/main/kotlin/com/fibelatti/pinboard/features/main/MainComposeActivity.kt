package com.fibelatti.pinboard.features.main

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.android.platform.BaseIntentBuilder
import com.fibelatti.core.android.platform.intentExtras
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val notificationManager: NotificationManager? get() = getSystemService()

    @Inject
    lateinit var unauthorizedPluginProvider: UnauthorizedPluginProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setThemedContent {
            MainScreen()
        }

        observeUnauthorized()

        intent.deeplinkNotificationId?.let { notificationId ->
            notificationManager?.cancel(notificationId)
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            mainViewModel.resetAppNavigation()
        }

        super.onDestroy()
    }

    private fun observeUnauthorized() {
        unauthorizedPluginProvider.unauthorized
            .onEach {
                window.decorView.findViewById<ViewGroup>(android.R.id.content)
                    ?.getChildAt(0)
                    ?.showBanner(messageRes = R.string.auth_logged_out_feedback)
            }
            .flowWithLifecycle(lifecycle = lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)
    }

    class Builder(context: Context) : BaseIntentBuilder(context, MainComposeActivity::class.java) {

        init {
            intent.fromBuilder = true
        }

        fun notificationExtras(postId: String, notificationId: Int): Builder = apply {
            intent.deeplinkPostId = postId
            intent.deeplinkNotificationId = notificationId
        }
    }

    private companion object {

        var Intent.fromBuilder by intentExtras(default = false)

        var Intent.deeplinkPostId: String? by intentExtras()
        var Intent.deeplinkNotificationId: Int? by intentExtras()
    }
}
