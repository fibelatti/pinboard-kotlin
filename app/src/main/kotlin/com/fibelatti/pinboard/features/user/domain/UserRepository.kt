package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow

@Suppress("ComplexInterface", "TooManyFunctions")
interface UserRepository {

    fun getLoginState(): Flow<LoginState>

    fun loginAttempt(authToken: String)

    fun loggedIn()

    fun logout()

    fun forceLogout()

    fun getLastUpdate(): String

    fun setLastUpdate(value: String)

    fun getAppearance(): Appearance

    fun setAppearance(appearance: Appearance)

    var preferredDateFormat: PreferredDateFormat

    fun getPreferredDetailsView(): PreferredDetailsView

    fun setPreferredDetailsView(preferredDetailsView: PreferredDetailsView)

    fun getMarkAsReadOnOpen(): Boolean

    fun setMarkAsReadOnOpen(value: Boolean)

    fun getAutoFillDescription(): Boolean

    fun setAutoFillDescription(value: Boolean)

    fun getShowDescriptionInLists(): Boolean

    fun setShowDescriptionInLists(value: Boolean)

    fun getDefaultPrivate(): Boolean?

    fun setDefaultPrivate(value: Boolean)

    fun getDefaultReadLater(): Boolean?

    fun setDefaultReadLater(value: Boolean)

    fun getEditAfterSharing(): EditAfterSharing

    fun setEditAfterSharing(editAfterSharing: EditAfterSharing)

    fun getDefaultTags(): List<Tag>

    fun setDefaultTags(tags: List<Tag>)
}
