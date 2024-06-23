package com.fibelatti.pinboard.features.user.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.fibelatti.bookmarking.core.ui.Appearance
import com.fibelatti.bookmarking.core.ui.PreferredDateFormat
import com.fibelatti.bookmarking.features.posts.domain.EditAfterSharing
import com.fibelatti.bookmarking.features.posts.domain.PreferredDetailsView
import com.fibelatti.bookmarking.features.sync.PeriodicSync
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.features.user.domain.UserPreferences

class UserPreferencesProvider : PreviewParameterProvider<UserPreferences> {

    override val values: Sequence<UserPreferences>
        get() = sequenceOf(
            UserPreferences(
                useLinkding = false,
                linkdingInstanceUrl = "",
                periodicSync = PeriodicSync.Every24Hours,
                appearance = Appearance.SystemDefault,
                applyDynamicColors = true,
                disableScreenshots = true,
                preferredDateFormat = PreferredDateFormat.YearMonthDayWithTime,
                preferredDetailsView = PreferredDetailsView.Edit,
                alwaysUseSidePanel = true,
                autoFillDescription = true,
                showDescriptionInLists = true,
                defaultPrivate = false,
                defaultReadLater = false,
                editAfterSharing = EditAfterSharing.AfterSaving,
                defaultTags = listOf(Tag(name = "Android")),
            ),
        )
}
