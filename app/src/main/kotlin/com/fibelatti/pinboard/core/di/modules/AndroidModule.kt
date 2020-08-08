package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.fibelatti.core.extension.getSystemService
import com.fibelatti.pinboard.core.persistence.getUserPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import java.text.Collator
import java.util.Locale

@Module
object AndroidModule {

    @Provides
    fun gson(): Gson = Gson()

    @Provides
    fun localeDefault(): Locale = Locale.getDefault()

    @Provides
    fun usCollator(): Collator = Collator.getInstance(Locale.US)

    @Provides
    fun sharedPreferences(
        application: Application
    ): SharedPreferences = application.getUserPreferences()

    @Provides
    fun connectivityManager(
        application: Application
    ): ConnectivityManager? = application.getSystemService()
}
