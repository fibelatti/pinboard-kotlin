package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.offline.domain.model.OfflineCopy
import javax.inject.Inject

class OfflineCopyActionHandler @Inject constructor() : ActionHandler<OfflineCopyAction>() {

    override suspend fun runAction(action: OfflineCopyAction, currentContent: Content): Content {
        return when (action) {
            is SetOfflineCopies -> setOfflineCopies(action, currentContent)
        }
    }

    private fun setOfflineCopies(action: SetOfflineCopies, currentContent: Content): Content {
        // Summed from the rows being shown rather than measured on disk: the list is scoped to one
        // account, so walking `filesDir` would report the space used by every account instead. It
        // also avoids a directory scan on every emission of the underlying query.
        val body = { offlineCopyListContent: OfflineCopyListContent ->
            offlineCopyListContent.copy(
                offlineCopies = action.offlineCopies,
                totalSize = action.offlineCopies.sumOf(OfflineCopy::sizeBytes),
                shouldLoad = false,
            )
        }

        return currentContent
            .reduce(body)
            .reduce<OfflineCopyDetailContent> { detailContent ->
                detailContent.copy(previousContent = body(detailContent.previousContent))
            }
    }
}
