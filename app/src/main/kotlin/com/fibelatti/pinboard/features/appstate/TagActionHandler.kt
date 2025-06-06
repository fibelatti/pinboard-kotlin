package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject

class TagActionHandler @Inject constructor(
    private val userRepository: UserRepository,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val getPreferredSortType: GetPreferredSortType,
) : ActionHandler<TagAction>() {

    override suspend fun runAction(action: TagAction, currentContent: Content): Content {
        return when (action) {
            is RefreshTags -> refresh(currentContent)
            is SetTags -> setTags(action, currentContent)
            is PostsForTag -> postsForTag(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return currentContent.reduce<TagListContent> { tagListContent ->
            tagListContent.copy(
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun setTags(action: SetTags, currentContent: Content): Content {
        return currentContent.reduce<TagListContent> { tagListContent ->
            tagListContent.copy(
                tags = action.tags,
                shouldLoad = false,
                previousContent = if (action.shouldReloadPosts) {
                    userRepository.lastUpdate = ""
                    tagListContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                } else {
                    tagListContent.previousContent
                },
            )
        }
    }

    private fun postsForTag(action: PostsForTag, currentContent: Content): Content {
        val body = { postListContent: PostListContent? ->
            PostListContent(
                category = All,
                // Use the current posts for a smoother transition until the tagged posts are loaded
                posts = postListContent?.posts,
                showDescription = userRepository.showDescriptionInLists,
                sortType = getPreferredSortType(),
                searchParameters = SearchParameters(tags = listOf(action.tag)),
                shouldLoad = ShouldLoadFirstPage,
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }

        return currentContent
            .reduce(body)
            .reduce<TagListContent> { tagListContent -> body(tagListContent.previousContent) }
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(
                    previousContent = body(postDetailContent.previousContent),
                )
            }
    }
}
