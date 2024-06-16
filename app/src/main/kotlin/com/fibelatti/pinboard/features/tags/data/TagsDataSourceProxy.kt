package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.linkding.data.TagsDataSourceLinkdingApi
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagsDataSourceProxy @Inject constructor(
    private val tagsDataSource: Lazy<TagsDataSource>,
    private val tagsDataSourceLinkdingApi: Lazy<TagsDataSourceLinkdingApi>,
    private val appModeProvider: AppModeProvider,
) : TagsRepository {

    private val repository: TagsRepository
        get() = if (AppMode.LINKDING == appModeProvider.appMode.value) {
            tagsDataSourceLinkdingApi.get()
        } else {
            tagsDataSource.get()
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
