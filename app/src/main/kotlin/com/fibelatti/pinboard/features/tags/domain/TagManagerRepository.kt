package com.fibelatti.pinboard.features.tags.domain

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagManagerRepository {

    val tagManagerState: Flow<TagManagerState>

    fun addTag(value: String)

    fun removeTag(tag: Tag)

    fun setTagSearchQuery(value: String)
}

@Stable
data class TagManagerState(
    val tags: List<Tag> = emptyList(),
    val suggestedTags: List<String> = emptyList(),
    val currentQuery: String = "",
) {

    @get:StringRes
    val displayTitle: Int
        get() = if (tags.isEmpty()) R.string.tags_empty_title else R.string.tags_added_title
}
