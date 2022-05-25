package com.fibelatti.pinboard.features

import android.content.Context
import android.content.pm.PackageInfo
import androidx.fragment.app.FragmentActivity
import com.fibelatti.core.extension.get
import com.fibelatti.core.extension.getSharedPreferences
import com.fibelatti.core.extension.put
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ActivityContext
import java.lang.reflect.Field
import javax.inject.Inject

class InAppReviewManager @Inject constructor(
    @ActivityContext context: Context,
) {

    companion object {

        private const val ONE_MONTH_IN_MILLIS: Long = 2_592_000_000

        private const val KEY_REVIEW_REQUESTED = "REVIEW_REQUESTED"
    }

    private val reviewManager = ReviewManagerFactory.create(context)
    private val reviewPreferences = context.getSharedPreferences("in_app_review")
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    fun checkForPlayStoreReview(activity: FragmentActivity) {
        val monthOldInstall = System.currentTimeMillis() - installTimeFromPackageManager() > ONE_MONTH_IN_MILLIS
        if (monthOldInstall && !reviewPreferences.get(KEY_REVIEW_REQUESTED, false)) {
            reviewPreferences.put(KEY_REVIEW_REQUESTED, true)

            reviewManager.requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    reviewManager.launchReviewFlow(activity, request.result)
                }
            }
        }
    }

    private fun installTimeFromPackageManager(): Long {
        return try {
            val info = packageManager.getPackageInfo(packageName, 0)
            val field: Field = PackageInfo::class.java.getField("firstInstallTime")
            return field.getLong(info)
        } catch (ignored: Exception) {
            0
        }
    }
}
