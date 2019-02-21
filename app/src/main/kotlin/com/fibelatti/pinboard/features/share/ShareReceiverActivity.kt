package com.fibelatti.pinboard.features.share

import android.content.Intent
import android.os.Bundle
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.extension.toast
import kotlinx.android.synthetic.main.activity_share.*

class ShareReceiverActivity : BaseActivity() {

    private val shareReceiverViewModel: ShareReceiverViewModel by lazy { viewModelFactory.get<ShareReceiverViewModel>(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        setupViewModels()
        checkForExtraText(intent, shareReceiverViewModel::saveUrl)
    }

    private fun setupViewModels() {
        with(shareReceiverViewModel) {
            observeEvent(saved) {
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                toast(it)
                finish()
            }
            observeEvent(failed) {
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved_error)
                toast(it)
                finish()
            }
        }
    }

    private fun checkForExtraText(intent: Intent?, onExtraTextFound: (String) -> Unit) {
        intent?.takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            ?.let(onExtraTextFound)
    }
}
