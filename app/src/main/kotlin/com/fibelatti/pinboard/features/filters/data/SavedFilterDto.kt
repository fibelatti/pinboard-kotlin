package com.fibelatti.pinboard.features.filters.data

import androidx.room.Entity
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.features.filters.data.SavedFilterDto.Companion.TABLE_NAME
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import org.koin.core.annotation.Factory

@Entity(
    tableName = TABLE_NAME,
    primaryKeys = ["term", "tags"],
)
data class SavedFilterDto(
    val term: String,
    val tags: String,
) {

    companion object {

        const val TABLE_NAME = "SavedFilters"
    }
}

@Factory
class SavedFilterDtoMapper : TwoWayMapper<SavedFilterDto, SavedFilter> {

    override fun map(param: SavedFilterDto): SavedFilter = SavedFilter(
        searchTerm = param.term,
        tags = param.tags.split(",").map(::Tag).filterNot { it.name.isBlank() },
    )

    override fun mapReverse(param: SavedFilter): SavedFilterDto = SavedFilterDto(
        term = param.searchTerm,
        tags = param.tags.joinToString(separator = ",") { it.name },
    )
}
