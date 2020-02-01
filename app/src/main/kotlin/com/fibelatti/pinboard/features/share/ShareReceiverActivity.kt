package com.fibelatti.pinboard.features.share

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.MainActivity
import kotlinx.android.synthetic.main.activity_share.*

class ShareReceiverActivity : BaseActivity(R.layout.activity_share) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val shareReceiverViewModel = viewModelFactory.get<ShareReceiverViewModel>(this)

        setupViewModels(shareReceiverViewModel)
        intent?.checkForExtraText(shareReceiverViewModel::saveUrl)
    }

    private fun setupViewModels(shareReceiverViewModel: ShareReceiverViewModel) {
        with(shareReceiverViewModel) {
            observeEvent(saved) { message ->
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                toast(message)
                finish()
            }
            observeEvent(edit) { message ->
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved)
                toast(message)
                startActivity(MainActivity.Builder(this@ShareReceiverActivity).build())
                finish()
            }
            observeEvent(failed) { message ->
                imageViewFeedback.setImageResource(R.drawable.ic_url_saved_error)
                toast(message)
                finish()
            }
        }
    }

    private fun Intent.checkForExtraText(onExtraTextFound: (String) -> Unit) {
        takeIf { it.action == Intent.ACTION_SEND && it.type == "text/plain" }
            ?.getStringExtra(Intent.EXTRA_TEXT)
            ?.let(onExtraTextFound)
    }
}
