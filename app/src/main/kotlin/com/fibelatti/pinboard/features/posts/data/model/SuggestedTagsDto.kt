package com.fibelatti.pinboard.features.posts.data.model

import androidx.annotation.Keep
import com.fibelatti.core.functional.Mapper
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

@Keep
data class SuggestedTagsDto(
    val popular: List<String>,
    val recommended: List<String>
)

class SuggestedTagDtoMapper @Inject constructor() : Mapper<SuggestedTagsDto, SuggestedTags> {

    override fun map(param: SuggestedTagsDto): SuggestedTags = with(param) {
        SuggestedTags(
            popular = popular.map { Tag(it) },
            recommended = recommended.map { Tag(it) }
        )
    }
}
