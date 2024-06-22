package com.fibelatti.pinboard

import com.fibelatti.pinboard.core.di.KoinGeneratedModule
import com.fibelatti.pinboard.core.di.androidAppModule
import com.fibelatti.pinboard.core.di.androidPlatformModule
import com.fibelatti.pinboard.core.di.coreModule
import com.fibelatti.pinboard.core.di.networkModule
import com.fibelatti.pinboard.core.di.testDatabaseModule
import com.fibelatti.pinboard.core.di.testLinkdingModule
import com.fibelatti.pinboard.core.di.testPinboardModule
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

            modules(
                coreModule,
                testDatabaseModule,
                networkModule,
                testPinboardModule,
                testLinkdingModule,
                androidPlatformModule,
                androidAppModule,
                KoinGeneratedModule().module,
            )
        }
    }
}
