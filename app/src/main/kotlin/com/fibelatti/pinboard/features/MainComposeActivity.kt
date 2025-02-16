package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.core.android.platform.BaseIntentBuilder
import com.fibelatti.core.android.platform.intentExtras
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.ContentWithHistory
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.NavigateBack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {

    private val appStateViewModel: AppStateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent()
    }

    private fun setContent() = setThemedContent {
        val content by appStateViewModel.content.collectAsStateWithLifecycle()
        val previousContent by remember {
            derivedStateOf { (content as? ContentWithHistory)?.previousContent }
        }

        BackHandler(enabled = previousContent !is ExternalContent) {
            appStateViewModel.runAction(NavigateBack)
        }

        MainScreen(
            content = content,
            onExternalBrowserContent = { browserContent ->
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(browserContent.post.url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )

                appStateViewModel.runAction(NavigateBack)
            },
            onExternalContent = {
                appStateViewModel.reset()
                finish()
            },
        )
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

    private companion object {

        var Intent.fromBuilder by intentExtras(default = false)
    }
}
