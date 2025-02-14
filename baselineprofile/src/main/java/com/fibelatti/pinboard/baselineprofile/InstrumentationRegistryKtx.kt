package com.fibelatti.pinboard.baselineprofile

import androidx.test.platform.app.InstrumentationRegistry

internal fun targetAppId(): String = InstrumentationRegistry.getArguments().getString("targetAppId")
    ?: error("targetAppId not passed as instrumentation runner arg")
