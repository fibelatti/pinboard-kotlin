package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.domain.TagManagerRepository
import com.fibelatti.pinboard.features.tags.domain.TagManagerState
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagManagerDataSource @Inject constructor(
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    postsRepository: PostsRepository,
) : TagManagerRepository {

    private val _tagManagerState: MutableStateFlow<TagManagerState?> = MutableStateFlow(null)
    override val tagManagerState: Flow<TagManagerState> = _tagManagerState.filterNotNull()

    init {
        scope.launch {
            appStateRepository.appState.collectLatest { appState ->
                when (appState.content) {
                    is AddPostContent -> {
                        _tagManagerState.update { TagManagerState(tags = appState.content.defaultTags) }
                    }

                    is EditPostContent -> {
                        _tagManagerState.update { TagManagerState(tags = appState.content.post.tags.orEmpty()) }
                    }

                    is UserPreferencesContent -> {
                        _tagManagerState.update { TagManagerState(tags = appState.content.userPreferences.defaultTags) }
                    }

                    else -> {
                        _tagManagerState.update { null }
                    }
                }
            }
        }

        scope.launch {
            tagManagerState.collectLatest { state ->
                postsRepository.searchExistingPostTag(tag = state.currentQuery, currentTags = state.tags)
                    .getOrNull()
                    ?.takeUnless { suggestedStags -> suggestedStags == state.suggestedTags }
                    ?.let { suggestedStags ->
                        _tagManagerState.update { current -> current?.copy(suggestedTags = suggestedStags) }
                    }
            }
        }
    }

    override fun addTag(value: String) {
        val currentTags = _tagManagerState.value?.tags.orEmpty().map { it.name }
        val newTags = value.trim()
            .split(" ")
            .filterNot { it in currentTags }
            .map(::Tag)

        _tagManagerState.update { current ->
            current?.copy(
                tags = current.tags + newTags,
                currentQuery = "",
            )
        }
    }

    override fun removeTag(tag: Tag) {
        _tagManagerState.update { current -> current?.copy(tags = current.tags.minus(tag)) }
    }

    override fun setTagSearchQuery(value: String) {
        _tagManagerState.update { current -> current?.copy(currentQuery = value) }
    }
}
