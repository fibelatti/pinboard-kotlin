package com.fibelatti.core.android.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * A default implementation of [ResourceProvider] to retrieve Android resources from a [Context].
 *
 * This class also registers a [BroadcastReceiver] to handle locale changes on its own, ensuring that the
 * provided resources are correctly localized.
 */
public class AppResourceProvider(context: Context) : ResourceProvider {

    private var context: Context = getLocalizedContext(context)

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctx: Context, intent: Intent) {
            this@AppResourceProvider.context = getLocalizedContext(ctx)
        }
    }

    init {
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
    }

    private fun getLocalizedContext(context: Context): Context {
        val config = Configuration(context.resources.configuration).apply {
            setLocale(Locale.getDefault())
        }
        return context.createConfigurationContext(config)
    }

    override fun getString(resId: Int): String = context.getString(resId)

    override fun getString(resId: Int, vararg formatArgs: Any): String = context.getString(resId, *formatArgs)

    override fun getJsonFromAssets(fileName: String): String? {
        return try {
            InputStreamReader(context.assets.open(fileName)).use { reader ->
                val stringBuilder = StringBuilder()
                val bufferedReader = BufferedReader(reader)
                var read = bufferedReader.readLine()
                while (read != null) {
                    stringBuilder.append(read)
                    read = bufferedReader.readLine()
                }
                stringBuilder.toString()
            }
        } catch (exception: Exception) {
            Log.d(TAG, "$TAG.getJsonFromAssets", exception)
            null
        }
    }

    private companion object {

        private const val TAG = "AppResourceProvider"
    }
}
