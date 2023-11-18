package com.fibelatti.pinboard.features.navigation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.ComposeBottomSheetDialog
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

object NavigationMenu {

    fun show(activity: AppCompatActivity) {
        ComposeBottomSheetDialog(activity) {
            NavigationMenuScreen(
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
                onLicensesClicked = {
                    activity.startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
                },
                onOptionSelected = { dismiss() },
            )
        }.show()
    }
}
