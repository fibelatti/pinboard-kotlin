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

@Singleton
internal class TagsDataSourceProxy @Inject constructor(
    private val tagsDataSource: Provider<TagsDataSource>,
    private val tagsDataSourceLinkdingApi: Provider<TagsDataSourceLinkdingApi>,
    private val appModeProvider: AppModeProvider,
) : TagsRepository {

    private var currentAppMode: AppMode? = null
    private var currentRepository: TagsRepository? = null

    private val repository: TagsRepository
        get() {
            val appMode = appModeProvider.appMode.value

            return currentRepository?.takeIf { currentAppMode == appMode }
                ?: if (AppMode.LINKDING == appModeProvider.appMode.value) {
                    tagsDataSourceLinkdingApi.get()
                } else {
                    tagsDataSource.get()
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
