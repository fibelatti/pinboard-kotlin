package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    val userCredentials: StateFlow<UserCredentials>

    val currentPreferences: StateFlow<UserPreferences>

    var linkdingInstanceUrl: String?

    var lastUpdate: String

    var periodicSync: PeriodicSync

    var appearance: Appearance

    var applyDynamicColors: Boolean

    var disableScreenshots: Boolean

    var preferredDateFormat: PreferredDateFormat

    var preferredSortType: SortType

    var hiddenPostQuickOptions: Set<String>

    var preferredDetailsView: PreferredDetailsView

    var useSplitNav: Boolean

    var markAsReadOnOpen: Boolean

    var followRedirects: Boolean

    var removeUtmParameters: Boolean

    var removedUrlParameters: Set<String>

    var autoFillDescription: Boolean

    var useBlockquote: Boolean

    var showDescriptionInLists: Boolean

    var defaultPrivate: Boolean?

    var defaultReadLater: Boolean?

    var editAfterSharing: EditAfterSharing

    var defaultTags: List<Tag>

    var alphabetizeTags: Boolean

    fun setAuthToken(appMode: AppMode, authToken: String)

    fun clearAuthToken(appMode: AppMode)
}
