package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.core.network.ConnectivityInfoProvider
import org.koin.core.annotation.Factory

@Factory
internal class PopularActionHandler(
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : ActionHandler<PopularAction>() {

    override suspend fun runAction(action: PopularAction, currentContent: Content): Content {
        return when (action) {
            is RefreshPopular -> refresh(currentContent)
            is SetPopularPosts -> setPosts(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        val body = { popularPostsContent: PopularPostsContent ->
            popularPostsContent.copy(
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }

        return currentContent
            .reduce(body)
            .reduce<PopularPostDetailContent> { popularPostDetailContent ->
                popularPostDetailContent.copy(
                    previousContent = body(popularPostDetailContent.previousContent),
                )
            }
    }

    private fun setPosts(action: SetPopularPosts, currentContent: Content): Content {
        val body = { popularPostsContent: PopularPostsContent ->
            popularPostsContent.copy(
                posts = action.posts,
                shouldLoad = false,
            )
        }

        return currentContent
            .reduce(body)
            .reduce<PopularPostDetailContent> { popularPostDetailContent ->
                popularPostDetailContent.copy(
                    previousContent = body(popularPostDetailContent.previousContent),
                )
            }
    }
}
