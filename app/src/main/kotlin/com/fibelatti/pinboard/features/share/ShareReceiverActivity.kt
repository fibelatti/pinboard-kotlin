package com.fibelatti.pinboard.features.share

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ShareCompat
import androidx.core.view.WindowCompat
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class ShareReceiverActivity : BaseActivity() {

    open val skipEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setThemedContent {
            ShareReceiverScreen(
                onEdit = {
                    startActivity(MainActivity.Builder(this).build())
                    finish()
                },
                onSaved = { finish() },
                errorDialogAction = { finish() },
            )
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val intentReader = ShareCompat.IntentReader(this)
        val url = intentReader.text.toString().ifEmpty {
            finish()
            return
        }

        val shareReceiverViewModel: ShareReceiverViewModel by viewModels()
        shareReceiverViewModel.saveUrl(
            url = url,
            title = intentReader.subject,
            skipEdit = skipEdit,
        )
    }
}
