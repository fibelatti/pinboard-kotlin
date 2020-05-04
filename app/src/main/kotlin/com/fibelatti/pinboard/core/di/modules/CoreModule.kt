package com.fibelatti.pinboard.core.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.fragment.app.FragmentFactory
import com.fibelatti.core.android.AppResourceProvider
import com.fibelatti.core.android.MultiBindingFragmentFactory
import com.fibelatti.core.extension.getSystemService
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.core.di.IoScope
import com.fibelatti.pinboard.core.persistence.getUserPreferences
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.text.Collator
import java.util.Locale

@Module
abstract class CoreModule {

    companion object {

        @Provides
        fun gson(): Gson = Gson()

        @Provides
        fun localeDefault(): Locale = Locale.getDefault()

        @Provides
        fun usCollator(): Collator = Collator.getInstance(Locale.US)

        @Provides
        fun Context.sharedPreferences(): SharedPreferences = getUserPreferences()

        @Provides
        fun Context.connectivityManager(): ConnectivityManager? = getSystemService()

        @Provides
        @IoScope
        fun ioScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @Binds
    abstract fun Application.context(): Context

    @Binds
    abstract fun AppResourceProvider.resourceProvider(): ResourceProvider

    @Binds
    abstract fun MultiBindingFragmentFactory.fragmentFactory(): FragmentFactory
}
