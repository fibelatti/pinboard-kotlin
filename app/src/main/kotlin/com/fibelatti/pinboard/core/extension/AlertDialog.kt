package com.fibelatti.pinboard.core.extension

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import org.koin.java.KoinJavaComponent.getKoin

fun AlertDialog.Builder.applySecureFlag(): AlertDialog = create().apply {

    setOnShowListener {
        val userRepository: UserRepository by getKoin().inject()
        if (userRepository.disableScreenshots) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }
    }
}
