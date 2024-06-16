package com.fibelatti.pinboard.tooling

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

@Suppress("Unused")
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        // Workaround to setup the MockWebServer
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
