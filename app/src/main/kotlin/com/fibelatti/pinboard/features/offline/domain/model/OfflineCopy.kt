package com.fibelatti.pinboard.features.offline.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.core.AppMode
import kotlinx.parcelize.Parcelize

@Stable
@Parcelize
data class OfflineCopy(
    val bookmarkId: String,
    val appMode: AppMode,
    val url: String,
    val title: String,
    val fileName: String,
    val sizeBytes: Long,
    val dateCreated: String,
    val truncated: Boolean,
) : Parcelable
