package com.fibelatti.pinboard.core.extension

import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

fun Context.materialAlertDialogBuilder(): MaterialAlertDialogBuilder {
    val entryPoint = EntryPointAccessors.fromApplication<DialogEntryPoint>(applicationContext)
    return if (entryPoint.userRepository().applyDynamicColors) {
        val wrappedContext = DynamicColors.wrapContextIfAvailable(this, R.style.AppTheme_MaterialDialog)
        MaterialAlertDialogBuilder(wrappedContext)
    } else {
        MaterialAlertDialogBuilder(this, R.style.AppTheme_MaterialDialog_DefaultColors)
    }
}

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

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface DialogEntryPoint {

    fun userRepository(): UserRepository
}
