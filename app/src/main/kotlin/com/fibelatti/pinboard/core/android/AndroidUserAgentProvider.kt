package com.fibelatti.pinboard.core.android

import android.os.Build
import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.network.UserAgentProvider
import javax.inject.Inject

class AndroidUserAgentProvider @Inject constructor() : UserAgentProvider {

    override val userAgent: String = "Pinkt/${BuildConfig.VERSION_NAME} (Android; ${Build.VERSION.SDK_INT})"
}
