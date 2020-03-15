package com.fibelatti.pinboard.features.navigation

import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialog
import com.fibelatti.core.extension.gone
import com.fibelatti.pinboard.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_menu.*

object NavigationDrawer {

    interface Callback {
        fun onAllClicked()
        fun onRecentClicked()
        fun onPublicClicked()
        fun onPrivateClicked()
        fun onUnreadClicked()
        fun onUntaggedClicked()
        fun onTagsClicked()
        fun onNotesClicked()
        fun onPopularClicked()
        fun onPreferencesClicked()
        fun onLogoutClicked()
        fun onShareAppClicked()
        fun onRateAppClicked()
    }

    fun show(context: Context, callback: Callback) {
        BottomSheetDialog(context, R.style.AppTheme_BaseBottomSheetDialog_BottomSheetDialog)
            .apply {
                setContentView(R.layout.fragment_menu)
                setupListeners(callback)
                setupVersion()
                setOnShowListener { dialog ->
                    (dialog as? BottomSheetDialog)?.apply {
                        window?.let {
                            it.decorView.systemUiVisibility = it.decorView.systemUiVisibility or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        }

                        findViewById<ViewGroup>(com.google.android.material.R.id.container)
                            ?.fitsSystemWindows = false

                        findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                            ?.let {
                                val behavior = BottomSheetBehavior.from(it)
                                behavior.skipCollapsed = true
                                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                            }
                    }
                }
                show()
            }
    }

    private fun AppCompatDialog.setupListeners(callback: Callback) {
        menuItemAll.setOnClickListener {
            callback.onAllClicked()
            dismiss()
        }
        menuItemRecent.setOnClickListener {
            callback.onRecentClicked()
            dismiss()
        }
        menuItemPublic.setOnClickListener {
            callback.onPublicClicked()
            dismiss()
        }
        menuItemPrivate.setOnClickListener {
            callback.onPrivateClicked()
            dismiss()
        }
        menuItemUnread.setOnClickListener {
            callback.onUnreadClicked()
            dismiss()
        }
        menuItemUntagged.setOnClickListener {
            callback.onUntaggedClicked()
            dismiss()
        }
        menuItemTags.setOnClickListener {
            callback.onTagsClicked()
            dismiss()
        }
        menuItemNotes.setOnClickListener {
            callback.onNotesClicked()
            dismiss()
        }
        menuItemPopular.setOnClickListener {
            callback.onPopularClicked()
            dismiss()
        }
        menuItemPreferences.setOnClickListener {
            callback.onPreferencesClicked()
            dismiss()
        }
        menuItemLogout.setOnClickListener {
            callback.onLogoutClicked()
            dismiss()
        }
        menuItemShare.setOnClickListener {
            callback.onShareAppClicked()
            dismiss()
        }
        menuItemRate.setOnClickListener {
            callback.onRateAppClicked()
            dismiss()
        }
    }

    private fun AppCompatDialog.setupVersion() {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            menuItemVersion.text = context.getString(R.string.about_version, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            menuItemVersion.gone()
        }
    }
}
