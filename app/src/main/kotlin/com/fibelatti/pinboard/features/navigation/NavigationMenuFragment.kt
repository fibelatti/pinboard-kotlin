package com.fibelatti.pinboard.features.navigation

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.di.AppReviewMode
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavigationMenuFragment @Inject constructor(
    @MainVariant private val mainVariant: Boolean,
    @AppReviewMode private val appReviewMode: Boolean,
    private val userRepository: UserRepository,
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = super.onCreateDialog(savedInstanceState).apply {
        if (userRepository.disableScreenshots) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }
    }

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

        return ComposeView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as ComposeView).setThemedContent {
            NavigationMenuScreen(
                mainVariant = mainVariant,
                appReviewMode = appReviewMode,
                onShareClicked = {
                    requireActivity().shareText(
                        R.string.share_title,
                        getString(
                            R.string.share_text,
                            "${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}",
                        ),
                    )
                },
                onRateClicked = {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("${AppConfig.PLAY_STORE_BASE_URL}${AppConfig.MAIN_PACKAGE_NAME}")
                        setPackage("com.android.vending")
                    }.let(::startActivity)
                },
                onOptionSelected = { dismiss() },
            )
        }
    }

    companion object {

        @JvmStatic
        val TAG: String = "NavigationMenuFragment"
    }
}
