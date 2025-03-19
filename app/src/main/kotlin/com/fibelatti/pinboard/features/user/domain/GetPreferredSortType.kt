package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateAddedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedOldestFirst
import com.fibelatti.pinboard.features.appstate.SortType
import javax.inject.Inject

class GetPreferredSortType @Inject constructor(
    private val userRepository: UserRepository,
    private val appModeProvider: AppModeProvider,
) {

    operator fun invoke(): SortType {
        val preferredSortType = userRepository.preferredSortType

        return if (appModeProvider.appMode.value == AppMode.LINKDING) {
            preferredSortType
        } else {
            when (preferredSortType) {
                is ByDateModifiedNewestFirst -> ByDateAddedNewestFirst
                is ByDateModifiedOldestFirst -> ByDateAddedOldestFirst
                else -> preferredSortType
            }
        }
    }
}
