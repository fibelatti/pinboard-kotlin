package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.linkding.data.TagsDataSourceLinkdingApi
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Singleton
internal class TagsDataSourceProxy @Inject constructor(
    private val tagsDataSourcePinboardApi: Provider<TagsDataSourcePinboardApi>,
    private val tagsDataSourceLinkdingApi: Provider<TagsDataSourceLinkdingApi>,
    private val tagsDataSourceNoApi: Provider<TagsDataSourceNoApi>,
    private val appModeProvider: AppModeProvider,
) : TagsRepository {

    private var currentAppMode: AppMode? = null
    private var currentRepository: TagsRepository? = null

    private val repository: TagsRepository
        get() = runBlocking {
            val appMode = appModeProvider.appMode.first { AppMode.UNSET != it }

            currentRepository?.takeIf { currentAppMode == appMode }
                ?: when (appMode) {
                    AppMode.NO_API -> tagsDataSourceNoApi.get()
                    AppMode.PINBOARD -> tagsDataSourcePinboardApi.get()
                    AppMode.LINKDING -> tagsDataSourceLinkdingApi.get()
                    AppMode.UNSET -> throw IllegalStateException()
                }.also {
                    currentAppMode = appMode
                    currentRepository = it
                }
        }

    override fun getAllTags(): Flow<Result<List<Tag>>> = repository.getAllTags()

    override suspend fun renameTag(
        oldName: String,
        newName: String,
    ): Result<List<Tag>> = repository.renameTag(
        oldName = oldName,
        newName = newName,
    )
}
