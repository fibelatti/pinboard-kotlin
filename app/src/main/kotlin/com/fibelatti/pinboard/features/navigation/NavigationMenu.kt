package com.fibelatti.pinboard.features.navigation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.ComposeBottomSheetDialog
import com.fibelatti.pinboard.core.di.AppReviewMode
import com.fibelatti.pinboard.core.di.MainVariant
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object NavigationMenu {

    fun show(activity: AppCompatActivity) {
        ComposeBottomSheetDialog(activity) {
            val entryPoint = EntryPointAccessors.fromApplication<NavigationMenuEntryPoint>(activity.applicationContext)

            NavigationMenuScreen(
                mainVariant = entryPoint.mainVariant(),
                appReviewMode = entryPoint.appReviewMode(),
                onShareClicked = {
                    activity.shareText(
                        R.string.share_title,
                        context.getString(
                            R.string.share_text,
                            "${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}",
                        ),
                    )
                },
                onRateClicked = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}")
                        setPackage("com.android.vending")
                    }

                    activity.startActivity(intent)
                },
                onOptionSelected = { dismiss() },
            )
        }.show()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NavigationMenuEntryPoint {

    @MainVariant
    fun mainVariant(): Boolean

    @AppReviewMode
    fun appReviewMode(): Boolean
}
