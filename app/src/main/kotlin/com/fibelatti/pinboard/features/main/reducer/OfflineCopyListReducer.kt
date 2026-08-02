package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.OfflineCopyListContent
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class OfflineCopyListReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val content: OfflineCopyListContent = appState.content as? OfflineCopyListContent ?: return mainState

        return MainState(
            title = MainState.TitleComponent.Visible(
                resourceProvider.getString(R.string.offline_copies_title),
            ),
            subtitle = when {
                content.shouldLoad || content.offlineCopies.isEmpty() -> MainState.TitleComponent.Gone

                else -> {
                    val quantityString = resourceProvider.getQuantityString(
                        R.plurals.offline_copies_quantity,
                        content.offlineCopies.size,
                        content.offlineCopies.size,
                    )
                    val formattedSize = resourceProvider.formatSizeBytes(content.totalSize)
                    val usageString = resourceProvider.getString(
                        R.string.offline_copies_total_size,
                        formattedSize,
                    )

                    MainState.TitleComponent.Visible("$quantityString · $usageString")
                }
            },
            navigation = MainState.NavigationComponent.Visible(),
        )
    }
}
