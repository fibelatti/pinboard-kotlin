package com.fibelatti.pinboard.features.filters.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.fibelatti.core.functional.TwoWayMapper
import com.fibelatti.pinboard.features.filters.data.SavedFilterDto.Companion.TABLE_NAME
import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

@Entity(
    tableName = TABLE_NAME,
    primaryKeys = ["term", "tags", "matchAll", "exactMatch"],
)
data class SavedFilterDto(
    val term: String,
    val tags: String,
    @ColumnInfo(defaultValue = "1")
    val matchAll: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val exactMatch: Boolean = false,
) {

    companion object {

        const val TABLE_NAME = "SavedFilters"
    }
}

class SavedFilterDtoMapper @Inject constructor() : TwoWayMapper<SavedFilterDto, SavedFilter> {

    override fun map(param: SavedFilterDto): SavedFilter = SavedFilter(
        term = param.term,
        tags = param.tags.split(",").map(::Tag).filterNot { it.name.isBlank() },
        matchAll = param.matchAll,
        exactMatch = param.exactMatch,
    )

    override fun mapReverse(param: SavedFilter): SavedFilterDto = SavedFilterDto(
        term = param.term,
        tags = param.tags.joinToString(separator = ",") { it.name },
        matchAll = param.matchAll,
        exactMatch = param.exactMatch,
    )
}
