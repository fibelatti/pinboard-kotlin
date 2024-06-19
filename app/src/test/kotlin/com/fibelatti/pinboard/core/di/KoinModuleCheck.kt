package com.fibelatti.pinboard.core.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkerParameters
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
internal class KoinModuleCheck {

    @Test
    fun checkKoinModule() {
        val testModule = module {
            includes(allModules())
        }

        testModule.verify(
            extraTypes = listOf(
                Context::class,
                HttpClientEngine::class,
                HttpClientConfig::class,
                SavedStateHandle::class,
                WorkerParameters::class,
            ),
        )
    }
}
