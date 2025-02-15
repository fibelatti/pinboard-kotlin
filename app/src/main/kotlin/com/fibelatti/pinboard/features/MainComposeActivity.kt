package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.core.android.platform.BaseIntentBuilder
import com.fibelatti.core.android.platform.intentExtras
import com.fibelatti.pinboard.core.android.WindowSizeClass
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ContentWithHistory
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.SidePanelContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {

    private val appStateViewModel: AppStateViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {
            appStateViewModel.runAction(NavigateBack)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setThemedContent {
            val mainState by mainViewModel.state.collectAsStateWithLifecycle()
            val content by appStateViewModel.content.collectAsStateWithLifecycle(
                minActiveState = Lifecycle.State.RESUMED,
            )
            val showSidePanel by remember {
                derivedStateOf {
                    content is SidePanelContent && mainState.multiPanelEnabled
                }
            }

            LaunchedEffect(content) {
                onBackPressedCallback.isEnabled = (content as? ContentWithHistory)?.previousContent !is ExternalContent
                mainViewModel.updateState { currentState ->
                    currentState.copy(multiPanelContent = content is SidePanelContent)
                }
            }

            MainScreen(
                content = content,
                showSidePanel = showSidePanel,
                onExternalBrowserContent = { ebc ->
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(ebc.post.url)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )

                    appStateViewModel.runAction(NavigateBack)
                },
                onExternalContent = {
                    appStateViewModel.reset()
                    finish()
                },
                onWindowSizeClassChange = { windowSizeClass ->
                    mainViewModel.updateState { currentState ->
                        currentState.copy(multiPanelEnabled = windowSizeClass != WindowSizeClass.COMPACT)
                    }
                },
            )
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            appStateViewModel.reset()
        }

        super.onDestroy()
    }

    class Builder(context: Context) : BaseIntentBuilder(context, MainComposeActivity::class.java) {

        init {
            intent.fromBuilder = true
        }
    }

    companion object {

        var Intent.fromBuilder by intentExtras(default = false)
    }
}
