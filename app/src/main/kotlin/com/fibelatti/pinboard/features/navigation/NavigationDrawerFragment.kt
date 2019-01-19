package com.fibelatti.pinboard.features.navigation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.pinboard.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_navigation.*

class NavigationDrawerFragment : BottomSheetDialogFragment() {

    interface Callback {
        fun onAllClicked()
        fun onRecentClicked()
        fun onPublicClicked()
        fun onPrivateClicked()
        fun onUnreadClicked()
        fun onUntaggedClicked()
        fun onTagsClicked()
        fun onLogoutClicked()
        fun onPreferencesClicked()
    }

    var callback: Callback? = null

    override fun getTheme(): Int = R.style.AppTheme_BaseBottomSheetDialog_BottomSheetDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_navigation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            callback?.run {
                when (menuItem.itemId) {
                    R.id.menuItemAll -> onAllClicked()
                    R.id.menuItemRecent -> onRecentClicked()
                    R.id.menuItemPublic -> onPublicClicked()
                    R.id.menuItemPrivate -> onPrivateClicked()
                    R.id.menuItemUnread -> onUnreadClicked()
                    R.id.menuItemUntagged -> onUntaggedClicked()
                    R.id.menuItemTags -> onTagsClicked()
                    R.id.menuItemLogout -> onLogoutClicked()
                    R.id.menuItemPreferences -> onPreferencesClicked()
                }
            }

            return@setNavigationItemSelectedListener true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }
}
