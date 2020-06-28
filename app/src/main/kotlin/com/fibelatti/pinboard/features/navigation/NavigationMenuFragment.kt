package com.fibelatti.pinboard.features.navigation

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.fibelatti.core.archcomponents.extension.activityViewModel
import com.fibelatti.core.archcomponents.extension.viewModel
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.di.ViewModelProvider
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_menu.*
import javax.inject.Inject

class NavigationMenuFragment @Inject constructor() : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        val TAG: String = "NavigationMenuFragment"
    }

    private val viewModelProvider: ViewModelProvider
        get() = (requireActivity() as BaseActivity).viewModelProvider
    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val authViewModel by viewModel { viewModelProvider.authViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_BottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener { dialog ->
            if (dialog !is BottomSheetDialog) {
                return@setOnShowListener
            }

            dialog.window?.let {
                it.decorView.systemUiVisibility = it.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }

            dialog.findViewById<ViewGroup>(com.google.android.material.R.id.container)
                ?.fitsSystemWindows = false

            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.skipCollapsed = true
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
        }

        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val pInfo = view.context.packageManager.getPackageInfo(view.context.packageName, 0)
            menuItemVersion.text = getString(R.string.about_version, pInfo.versionName)
        } catch (ignored: PackageManager.NameNotFoundException) {
            menuItemVersion.gone()
        }

        setupCategoryListeners()
        setupUserListeners()
        setupFooterListeners()
    }

    private fun setupCategoryListeners() {
        menuItemAll.setOnClickListener {
            appStateViewModel.runAction(All)
            dismiss()
        }
        menuItemRecent.setOnClickListener {
            appStateViewModel.runAction(Recent)
            dismiss()
        }
        menuItemPublic.setOnClickListener {
            appStateViewModel.runAction(Public)
            dismiss()
        }
        menuItemPrivate.setOnClickListener {
            appStateViewModel.runAction(Private)
            dismiss()
        }
        menuItemUnread.setOnClickListener {
            appStateViewModel.runAction(Unread)
            dismiss()
        }
        menuItemUntagged.setOnClickListener {
            appStateViewModel.runAction(Untagged)
            dismiss()
        }
        menuItemTags.setOnClickListener {
            appStateViewModel.runAction(ViewTags)
            dismiss()
        }
        menuItemNotes.setOnClickListener {
            appStateViewModel.runAction(ViewNotes)
            dismiss()
        }
        menuItemPopular.setOnClickListener {
            appStateViewModel.runAction(ViewPopular)
            dismiss()
        }
    }

    private fun setupUserListeners() {
        menuItemPreferences.setOnClickListener {
            appStateViewModel.runAction(ViewPreferences)
            dismiss()
        }
        menuItemLogout.setOnClickListener {
            authViewModel.logout()
            dismiss()
        }
    }

    private fun setupFooterListeners() {
        menuItemShare.setOnClickListener {
            requireActivity().shareText(
                R.string.share_title,
                getString(
                    R.string.share_text,
                    "${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}"
                )
            )
            dismiss()
        }
        menuItemRate.setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}")
                setPackage("com.android.vending")
            }.let(::startActivity)
            dismiss()
        }
    }
}
