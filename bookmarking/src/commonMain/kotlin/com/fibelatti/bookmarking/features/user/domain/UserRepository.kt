package com.fibelatti.bookmarking.features.user.domain

import com.fibelatti.bookmarking.core.ui.Appearance
import com.fibelatti.bookmarking.core.ui.PreferredDateFormat
import com.fibelatti.bookmarking.features.posts.domain.EditAfterSharing
import com.fibelatti.bookmarking.features.posts.domain.PreferredDetailsView
import com.fibelatti.bookmarking.features.sync.PeriodicSync
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.StateFlow

public interface UserRepository {

    public val currentPreferences: StateFlow<UserPreferences>

    public var useLinkding: Boolean

    public var linkdingInstanceUrl: String

    public var lastUpdate: String

    public var periodicSync: PeriodicSync

    public var appearance: Appearance

    public var applyDynamicColors: Boolean

    public var disableScreenshots: Boolean

    public var preferredDateFormat: PreferredDateFormat

    public var preferredDetailsView: PreferredDetailsView

    public var alwaysUseSidePanel: Boolean

    public var markAsReadOnOpen: Boolean

    public var autoFillDescription: Boolean

    public var showDescriptionInLists: Boolean

    public var defaultPrivate: Boolean?

    public var defaultReadLater: Boolean?

    public var editAfterSharing: EditAfterSharing

    public var defaultTags: List<Tag>

    public fun getUsername(): String

    public fun hasAuthToken(): Boolean

    public fun setAuthToken(authToken: String)

    public fun clearAuthToken()
}
