package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.extension.orFalse
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject

class PostsDataSource @Inject constructor(
    private val userRepository: UserRepository,
    private val postsApi: PostsApi,
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val suggestedTagDtoMapper: SuggestedTagDtoMapper,
    private val dateFormatter: DateFormatter
) : PostsRepository {

    override suspend fun update(): Result<String> =
        resultFrom { postsApi.update().await() }
            .mapCatching { it.updateTime }

    override suspend fun add(
        url: String,
        description: String,
        extended: String?,
        tags: List<String>?
    ): Result<Unit> =
        resultFrom { postsApi.add(url, description, extended, tags?.forRequest()).await() }
            .orThrow()

    override suspend fun delete(
        url: String
    ): Result<Unit> =
        resultFrom { postsApi.delete(url).await() }
            .orThrow()

    override suspend fun getRecentPosts(
        tags: List<String>?
    ): Result<List<Post>> =
        resultFrom { postsApi.getRecentPosts(tags?.forRequest()).await() }
            .mapCatching { postDtoMapper.mapList(it.posts) }

    override suspend fun getAllPosts(tags: List<String>?): Result<List<Post>> =
        withLocalDataSourceCheck { postsApi.getAllPosts(tags?.forRequest()).await() }
            .mapCatching(postDtoMapper::mapList)

    override suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags> =
        resultFrom { postsApi.getSuggestedTagsForUrl(url).await() }
            .mapCatching(suggestedTagDtoMapper::map)

    private fun Result<GenericResponseDto>.orThrow() = mapCatching {
        if (it.resultCode != ApiResultCodes.DONE.code) throw ApiException()
    }

    private fun List<String>.forRequest() = run {
        joinToString(AppConfig.PinboardApiLiterals.TAG_SEPARATOR_REQUEST)
    }

    private suspend inline fun withLocalDataSourceCheck(
        crossinline onInvalidLocalData: suspend () -> List<PostDto>
    ): Result<List<PostDto>> {
        val userLastUpdate = userRepository.getLastUpdate()
        val apiLastUpdate = update().getOrNull() ?: dateFormatter.nowAsTzFormat()
        val localPosts = catching { postsDao.getAllPosts() }

        return if (userLastUpdate == apiLastUpdate && localPosts.getOrNull()?.isNotEmpty().orFalse()) {
            localPosts
        } else {
            resultFrom { onInvalidLocalData() }
                .onSuccess {
                    catching {
                        userRepository.setLastUpdate(apiLastUpdate)
                        postsDao.deleteAllPosts()
                        postsDao.savePosts(it)
                    }
                }
        }
    }
}
