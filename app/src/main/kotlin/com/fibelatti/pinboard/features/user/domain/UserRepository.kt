package com.fibelatti.pinboard.features.user.domain

import androidx.lifecycle.LiveData
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView

@Suppress("ComplexInterface", "TooManyFunctions")
interface UserRepository {

    fun getLoginState(): LiveData<LoginState>

    fun loginAttempt(authToken: String)

    fun loggedIn()

    fun logout()

    fun forceLogout()

    fun getLastUpdate(): String

    fun setLastUpdate(value: String)

    fun getAppearance(): Appearance

    fun setAppearance(appearance: Appearance)

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

    fun getEditAfterSharing(): Boolean

    fun setEditAfterSharing(value: Boolean)
}
