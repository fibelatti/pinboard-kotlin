package com.fibelatti.pinboard.features.share

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickShareReceiverActivity : ShareReceiverActivity() {

    override val skipEdit: Boolean = true
}
