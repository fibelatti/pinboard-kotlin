package com.fibelatti.pinboard.features.share

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.fibelatti.core.android.platform.intentExtras
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.main.MainComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val shareReceiverViewModel: ShareReceiverViewModel by viewModels()

        setThemedContent {
            ShareReceiverScreen(
                onEdit = {
                    startActivity(MainComposeActivity.Builder(this).build())
                    finish()
                },
                onSaved = ::finish,
                onSelectService = shareReceiverViewModel::selectService,
                errorDialogAction = ::finish,
            )
        }

        val intentReader = ShareCompat.IntentReader(this)
        val url = intentReader.text.toString().ifEmpty {
            finish()
            return
        }

        shareReceiverViewModel.saveUrl(
            url = url,
            title = intentReader.subject,
            skipEdit = intent.skipEdit == true,
        )
    }

    companion object {

        private var Intent.skipEdit: Boolean? by intentExtras()

        fun quickShareIntent(context: Context, intent: Intent): Intent {
            return Intent(intent).apply {
                setClass(context, ShareReceiverActivity::class.java)
                skipEdit = true
            }
        }
    }
}
