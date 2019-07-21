package com.fibelatti.pinboard.features.navigation

import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import com.fibelatti.core.extension.gone
import com.fibelatti.pinboard.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_menu.*

class NavigationDrawerFragment {

    interface Callback {
        fun onAllClicked()
        fun onRecentClicked()
        fun onPublicClicked()
        fun onPrivateClicked()
        fun onUnreadClicked()
        fun onUntaggedClicked()
        fun onTagsClicked()
        fun onNotesClicked()
        fun onLogoutClicked()
        fun onShareAppClicked()
        fun onRateAppClicked()
    }

    fun showNavigation(context: Context) {
        BottomSheetDialog(context, R.style.AppTheme_BaseBottomSheetDialog_BottomSheetDialog).apply {
            setContentView(R.layout.fragment_menu)
            (context as? Callback)?.let { setupListeners(it) }
            setupVersion()
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
        } catch (e: Exception) {
            menuItemVersion.gone()
        }
    }
}
