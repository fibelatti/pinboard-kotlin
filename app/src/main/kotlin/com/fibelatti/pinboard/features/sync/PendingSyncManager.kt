package com.fibelatti.pinboard.features.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val connectivityManager: ConnectivityManager? get() = context.getSystemService()
    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            enqueueWork()
        }

        override fun onLost(network: Network) {
            cancelWork()
        }
    }

    private val processObserver = LifecycleEventObserver { _, event ->
        if (Lifecycle.Event.ON_START == event) {
            enqueueWork()
        }
    }

    fun setupListeners() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)

        ProcessLifecycleOwner.get().lifecycle.addObserver(processObserver)
    }

    private fun enqueueWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val work = OneTimeWorkRequestBuilder<PendingSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(PendingSyncWorker.UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, work)
    }

    private fun cancelWork() {
        workManager.cancelUniqueWork(PendingSyncWorker.UNIQUE_WORK_NAME)
    }
}
