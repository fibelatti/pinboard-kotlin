package com.fibelatti.pinboard.features

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InAppUpdateManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private companion object {

        private const val FLEXIBLE_UPDATE_REQUEST = 1001
    }

    private val inAppUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var currentAppUpdateInfo: AppUpdateInfo? = null

    suspend fun isUpdateAvailable(): Boolean = suspendCoroutine { continuation ->
        inAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            currentAppUpdateInfo = appUpdateInfo

            val updateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED

            continuation.resume(updateAvailable)
        }
    }

    suspend fun downloadUpdate(fragmentActivity: FragmentActivity) = suspendCoroutine { continuation ->
        val appUpdateInfo = requireNotNull(currentAppUpdateInfo)

        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            continuation.resume(Unit)
        } else {
            inAppUpdateManager.registerListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    continuation.resume(Unit)
                }
            }
            inAppUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                fragmentActivity,
                FLEXIBLE_UPDATE_REQUEST,
            )
        }
    }

    fun installUpdate() {
        inAppUpdateManager.completeUpdate()
    }
}
