package com.fibelatti.pinboard.tooling

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.test.runner.AndroidJUnitRunner

@Suppress("Unused")
class InstrumentationTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        // Workaround to setup the MockWebServer
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        return super.newApplication(cl, TestApp::class.java.name, context)
    }
}
