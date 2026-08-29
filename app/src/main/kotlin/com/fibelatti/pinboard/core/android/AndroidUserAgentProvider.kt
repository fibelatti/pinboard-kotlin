package com.fibelatti.pinboard.core.android

import android.os.Build
import com.fibelatti.core.platform.UserAgentProvider
import com.fibelatti.pinboard.BuildConfig
import javax.inject.Inject

class AndroidUserAgentProvider @Inject constructor() : UserAgentProvider {

    override val userAgent: String = "Pinkt/${BuildConfig.VERSION_NAME} (Android; ${Build.VERSION.SDK_INT})"
}
