package com.fibelatti.pinboard.core.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.fibelatti.pinboard.core.extension.getUserPreferences
import dagger.Module
import dagger.Provides

@Module
abstract class PreferencesModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getUserPreferences()
    }
}
