package com.fibelatti.pinboard.core.extension

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

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
