package com.fibelatti.pinboard.features.user.presentation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserPreferences

class UserPreferencesProvider : PreviewParameterProvider<UserPreferences> {

    override val values: Sequence<UserPreferences>
        get() = sequenceOf(
            UserPreferences(
                periodicSync = PeriodicSync.Every24Hours,
                appearance = Appearance.SystemDefault,
                applyDynamicColors = true,
                disableScreenshots = true,
                preferredDateFormat = PreferredDateFormat.YearMonthDayWithTime,
                preferredSortType = ByDateAddedNewestFirst,
                hiddenPostQuickOptions = emptySet(),
                preferredDetailsView = PreferredDetailsView.Edit,
                followRedirects = true,
                removeUtmParameters = true,
                removedUrlParameters = emptySet(),
                autoFillDescription = true,
                useBlockquote = true,
                showDescriptionInLists = true,
                defaultPrivate = false,
                defaultReadLater = false,
                editAfterSharing = EditAfterSharing.AfterSaving,
                defaultTags = listOf(Tag(name = "Android")),
            ),
        )
}
