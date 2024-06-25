package com.fibelatti.bookmarking.features.filters.data

import androidx.room.Entity
import com.fibelatti.bookmarking.features.filters.data.SavedFilterDto.Companion.TABLE_NAME
import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.TwoWayMapper
import org.koin.core.annotation.Factory

@Entity(
    tableName = TABLE_NAME,
    primaryKeys = ["term", "tags"],
)
internal data class SavedFilterDto(
    val term: String,
    val tags: String,
) {

    public companion object {

        public const val TABLE_NAME: String = "SavedFilters"
    }
}

@Factory
internal class SavedFilterDtoMapper : TwoWayMapper<SavedFilterDto, SavedFilter> {

    override fun map(param: SavedFilterDto): SavedFilter = SavedFilter(
        searchTerm = param.term,
        tags = param.tags.split(",").map(::Tag).filterNot { it.name.isBlank() },
    )

    override fun mapReverse(param: SavedFilter): SavedFilterDto = SavedFilterDto(
        term = param.searchTerm,
        tags = param.tags.joinToString(separator = ",") { it.name },
    )
}
