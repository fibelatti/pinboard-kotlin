package com.fibelatti.pinboard.features.tags.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Tag(val name: String, val posts: Int) : Parcelable {

    constructor(name: String) : this(name, 0)

    override fun equals(other: Any?): Boolean = name == (other as? Tag)?.name

    override fun hashCode(): Int = name.hashCode() * 31
}
