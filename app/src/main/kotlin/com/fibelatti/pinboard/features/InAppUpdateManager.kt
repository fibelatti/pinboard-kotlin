package com.fibelatti.pinboard.features

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class InAppUpdateManager @Inject constructor(
    @ActivityContext context: Context,
) {

    private companion object {

        private const val FLEXIBLE_UPDATE_REQUEST = 1001
    }

    private val inAppUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    fun checkForAvailableUpdates(
        activity: FragmentActivity,
        onDownloadComplete: () -> Unit,
    ) {
        val installStateUpdatedListener = object : InstallStateUpdatedListener {

            override fun onStateUpdate(state: InstallState) {
                val shouldNotify = state.installStatus() == InstallStatus.DOWNLOADED &&
                    activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

                if (shouldNotify) {
                    inAppUpdateManager.unregisterListener(this)
                    onDownloadComplete.invoke()
                }
            }
        }

        val lifecycleObserver = object : DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                inAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    when (appUpdateInfo.installStatus()) {
                        InstallStatus.DOWNLOADED -> onDownloadComplete.invoke()
                        InstallStatus.INSTALLING, InstallStatus.INSTALLED,
                        InstallStatus.FAILED, InstallStatus.CANCELED,
                        -> owner.lifecycle.removeObserver(this)
                        else -> Unit
                    }
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                inAppUpdateManager.unregisterListener(installStateUpdatedListener)
            }
        }

        inAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val updateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            if (updateAvailable) {
                activity.lifecycle.addObserver(lifecycleObserver)
                inAppUpdateManager.registerListener(installStateUpdatedListener)
                inAppUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    activity,
                    FLEXIBLE_UPDATE_REQUEST
                )
            }
        }
    }

    fun completeUpdate() {
        inAppUpdateManager.completeUpdate()
    }
}
