package com.fibelatti.pinboard.tooling

import com.fibelatti.bookmarking.di.GeneratedBookmarkingModule
import com.fibelatti.pinboard.App
import com.fibelatti.pinboard.core.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

class TestApp : App() {

    override fun setupDependencyGraph() {
        startKoin {
            androidLogger()
            androidContext(this@TestApp)
            fragmentFactory()
            workManagerFactory()

            val allModules = appModules() +
                testBookmarkingModules() +
                GeneratedBookmarkingModule().module

            modules(allModules)
        }
    }
}
