package com.fibelatti.pinboard.core.android

import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.setThemedContent
import com.fibelatti.pinboard.core.extension.setViewTreeOwners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.java.KoinJavaComponent.getKoin

class ComposeBottomSheetDialog(
    context: Context,
    content: @Composable BottomSheetDialog.() -> Unit,
) : BottomSheetDialog(context, R.style.AppTheme_BottomSheetDialog) {

    private val userRepository: UserRepository by getKoin().inject()

    init {
        if (userRepository.disableScreenshots) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }

        behavior.peekHeight = 1200.dp.value.toInt()
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        setViewTreeOwners()

        setContentView(
            ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setThemedContent {
                    content()
                }
            },
        )
    }
}
