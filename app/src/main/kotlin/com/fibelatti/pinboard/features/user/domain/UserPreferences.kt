package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag

data class UserPreferences(
    val useLinkding: Boolean,
    val linkdingInstanceUrl: String,
    val periodicSync: PeriodicSync,
    val appearance: Appearance,
    val applyDynamicColors: Boolean,
    val disableScreenshots: Boolean,
    val preferredDateFormat: PreferredDateFormat,
    val preferredDetailsView: PreferredDetailsView,
    val alwaysUseSidePanel: Boolean,
    val followRedirects: Boolean,
    val autoFillDescription: Boolean,
    val showDescriptionInLists: Boolean,
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val editAfterSharing: EditAfterSharing,
    val defaultTags: List<Tag>,
)
