package com.fibelatti.pinboard.features.tags.domain.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Tag(val name: String, val posts: Int) : Parcelable {

    constructor(name: String) : this(name, 0)

    override fun equals(other: Any?): Boolean = name == (other as? Tag)?.name

    override fun hashCode(): Int = name.hashCode() * 31
}
