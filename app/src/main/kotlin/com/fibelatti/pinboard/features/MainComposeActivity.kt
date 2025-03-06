package com.fibelatti.pinboard.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fibelatti.core.android.platform.BaseIntentBuilder
import com.fibelatti.core.android.platform.intentExtras
import com.fibelatti.pinboard.core.extension.setThemedContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainComposeActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setThemedContent {
            MainScreen()
        }
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            mainViewModel.resetAppNavigation()
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
