package com.fibelatti.pinboard.features.navigation

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.fibelatti.core.extension.shareText
import com.fibelatti.core.extension.viewBinding
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.di.AppReviewMode
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.databinding.FragmentMenuBinding
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavigationMenuFragment @Inject constructor(
    @MainVariant private val mainVariant: Boolean,
    @AppReviewMode private val appReviewMode: Boolean,
) : BottomSheetDialogFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "NavigationMenuFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private val binding by viewBinding(FragmentMenuBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        dialog?.setOnShowListener { dialog ->
            if (dialog !is BottomSheetDialog) {
                return@setOnShowListener
            }

            dialog.window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }

            dialog.findViewById<ViewGroup>(com.google.android.material.R.id.container)
                ?.fitsSystemWindows = false

            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.skipCollapsed = true
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
        }

        return FragmentMenuBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            @Suppress("DEPRECATION")
            val pInfo = view.context.packageManager.getPackageInfo(view.context.packageName, 0)
            binding.menuItemVersion.text = getString(R.string.about_version, pInfo.versionName)
        } catch (ignored: PackageManager.NameNotFoundException) {
            binding.menuItemVersion.isGone = true
        }

        binding.menuItemPublic.isVisible = mainVariant
        binding.menuItemPrivate.isVisible = mainVariant
        binding.menuItemNotes.isVisible = mainVariant && !appReviewMode
        binding.menuItemLogout.isVisible = mainVariant

        setupCategoryListeners()
        setupUserListeners()
        setupFooterListeners()
    }

    private fun setupCategoryListeners() {
        binding.menuItemAll.setOnClickListener {
            appStateViewModel.runAction(All)
            dismiss()
        }
        binding.menuItemRecent.setOnClickListener {
            appStateViewModel.runAction(Recent)
            dismiss()
        }
        binding.menuItemPublic.setOnClickListener {
            appStateViewModel.runAction(Public)
            dismiss()
        }
        binding.menuItemPrivate.setOnClickListener {
            appStateViewModel.runAction(Private)
            dismiss()
        }
        binding.menuItemUnread.setOnClickListener {
            appStateViewModel.runAction(Unread)
            dismiss()
        }
        binding.menuItemUntagged.setOnClickListener {
            appStateViewModel.runAction(Untagged)
            dismiss()
        }
        binding.menuItemTags.setOnClickListener {
            appStateViewModel.runAction(ViewTags)
            dismiss()
        }
        binding.menuItemNotes.setOnClickListener {
            appStateViewModel.runAction(ViewNotes)
            dismiss()
        }
        binding.menuItemPopular.setOnClickListener {
            appStateViewModel.runAction(ViewPopular)
            dismiss()
        }
    }

    private fun setupUserListeners() {
        binding.menuItemPreferences.setOnClickListener {
            appStateViewModel.runAction(ViewPreferences)
            dismiss()
        }
        binding.menuItemLogout.setOnClickListener {
            authViewModel.logout()
            dismiss()
        }
    }

    private fun setupFooterListeners() {
        binding.menuItemShare.setOnClickListener {
            requireActivity().shareText(
                R.string.share_title,
                getString(
                    R.string.share_text,
                    "${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}"
                )
            )
            dismiss()
        }
        binding.menuItemRate.setOnClickListener {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}")
                setPackage("com.android.vending")
            }.let(::startActivity)
            dismiss()
        }
    }
}
