package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.linkding.data.TagsDataSourceLinkdingApi
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class TagsDataSourceProxy(
    private val tagsDataSource: TagsDataSource,
    private val tagsDataSourceLinkdingApi: TagsDataSourceLinkdingApi,
    private val appModeProvider: AppModeProvider,
) : TagsRepository {

    private val repository: TagsRepository
        get() = if (AppMode.LINKDING == appModeProvider.appMode.value) {
            tagsDataSourceLinkdingApi
        } else {
            tagsDataSource
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
