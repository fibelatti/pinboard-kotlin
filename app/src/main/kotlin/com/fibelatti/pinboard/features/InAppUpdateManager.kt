package com.fibelatti.pinboard.features

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.fibelatti.pinboard.core.functional.DoNothing
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import javax.inject.Inject

class InAppUpdateManager @Inject constructor(context: Context) : InstallStateUpdatedListener,
    LifecycleEventObserver {

    private val inAppUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private var lifecycle: Lifecycle? = null
    private var onDownloadComplete: (() -> Unit)? = null

    fun checkForAvailableUpdates(
        activity: FragmentActivity,
        requestCode: Int,
        onDownloadComplete: () -> Unit
    ) {
        inAppUpdateManager.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                this.lifecycle = activity.lifecycle.also { it.addObserver(this) }
                this.onDownloadComplete = onDownloadComplete

                inAppUpdateManager.registerListener(this)
                inAppUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    activity,
                    requestCode
                )
            }
        }
    }

    fun completeUpdate() {
        inAppUpdateManager.unregisterListener(this)
        inAppUpdateManager.completeUpdate()
    }

    override fun onStateUpdate(state: InstallState?) {
        if (state?.installStatus() == InstallStatus.DOWNLOADED) {
            onDownloadComplete?.invoke()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                inAppUpdateManager.appUpdateInfo?.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        onDownloadComplete?.invoke()
                    }
                }
            }
            Lifecycle.Event.ON_DESTROY -> {
                lifecycle = null
                onDownloadComplete = null
                inAppUpdateManager.unregisterListener(this)
            }
            else -> DoNothing
        }
    }
}
