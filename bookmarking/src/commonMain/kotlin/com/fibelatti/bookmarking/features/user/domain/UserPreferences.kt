package com.fibelatti.bookmarking.features.user.domain

import com.fibelatti.bookmarking.core.ui.Appearance
import com.fibelatti.bookmarking.core.ui.PreferredDateFormat
import com.fibelatti.bookmarking.features.posts.domain.EditAfterSharing
import com.fibelatti.bookmarking.features.posts.domain.PreferredDetailsView
import com.fibelatti.bookmarking.features.sync.PeriodicSync
import com.fibelatti.bookmarking.features.tags.domain.model.Tag

public data class UserPreferences(
    val useLinkding: Boolean,
    val linkdingInstanceUrl: String,
    val periodicSync: PeriodicSync,
    val appearance: Appearance,
    val applyDynamicColors: Boolean,
    val disableScreenshots: Boolean,
    val preferredDateFormat: PreferredDateFormat,
    val preferredDetailsView: PreferredDetailsView,
    val alwaysUseSidePanel: Boolean,
    val autoFillDescription: Boolean,
    val showDescriptionInLists: Boolean,
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val editAfterSharing: EditAfterSharing,
    val defaultTags: List<Tag>,
)
