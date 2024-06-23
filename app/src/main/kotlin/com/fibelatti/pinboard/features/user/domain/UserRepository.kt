package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.bookmarking.core.ui.Appearance
import com.fibelatti.bookmarking.core.ui.PreferredDateFormat
import com.fibelatti.bookmarking.features.posts.domain.EditAfterSharing
import com.fibelatti.bookmarking.features.posts.domain.PreferredDetailsView
import com.fibelatti.bookmarking.features.sync.PeriodicSync
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    val currentPreferences: StateFlow<UserPreferences>

    var useLinkding: Boolean

    var linkdingInstanceUrl: String

    var lastUpdate: String

    var periodicSync: PeriodicSync

    var appearance: Appearance

    var applyDynamicColors: Boolean

    var disableScreenshots: Boolean

    var preferredDateFormat: PreferredDateFormat

    var preferredDetailsView: PreferredDetailsView

    var alwaysUseSidePanel: Boolean

    var markAsReadOnOpen: Boolean

    var autoFillDescription: Boolean

    var showDescriptionInLists: Boolean

    var defaultPrivate: Boolean?

    var defaultReadLater: Boolean?

    var editAfterSharing: EditAfterSharing

    var defaultTags: List<Tag>

    fun getUsername(): String

    fun hasAuthToken(): Boolean

    fun setAuthToken(authToken: String)

    fun clearAuthToken()
}
