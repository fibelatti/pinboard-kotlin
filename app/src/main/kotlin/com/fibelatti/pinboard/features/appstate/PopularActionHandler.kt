package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import javax.inject.Inject

class PopularActionHandler @Inject constructor(
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : ActionHandler<PopularAction>() {

    override suspend fun runAction(action: PopularAction, currentContent: Content): Content {
        return when (action) {
            is RefreshPopular -> refresh(currentContent)
            is SetPopularPosts -> setPosts(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PopularPostsContent>(currentContent) {
            it.copy(
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun setPosts(action: SetPopularPosts, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PopularPostsContent>(currentContent) {
            it.copy(
                posts = action.posts,
                shouldLoad = false,
            )
        }
    }
}
