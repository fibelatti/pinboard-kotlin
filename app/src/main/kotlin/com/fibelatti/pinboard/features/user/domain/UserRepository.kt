package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val currentPreferences: Flow<UserPreferences>

    var lastUpdate: String

    var autoUpdate: Boolean

    var periodicSync: PeriodicSync

    var appearance: Appearance

    var applyDynamicColors: Boolean

    var preferredDateFormat: PreferredDateFormat

    var preferredDetailsView: PreferredDetailsView

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
