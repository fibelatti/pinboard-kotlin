package com.fibelatti.pinboard.core.extension

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.fibelatti.pinboard.core.android.DialogEntryPoint
import dagger.hilt.android.EntryPointAccessors

fun AlertDialog.Builder.applySecureFlag(): AlertDialog = create().apply {
    setOnShowListener {
        val entryPoint = EntryPointAccessors.fromApplication<DialogEntryPoint>(context.applicationContext)

        if (entryPoint.userRepository().disableScreenshots) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        }
    }
}
