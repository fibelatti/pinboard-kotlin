package com.fibelatti.bookmarking.features.tags.domain.model

import kotlinx.serialization.Serializable

@Serializable
public data class Tag(val name: String, val posts: Int) {

    public constructor(name: String) : this(name, 0)

    override fun equals(other: Any?): Boolean = name == (other as? Tag)?.name

    override fun hashCode(): Int = name.hashCode() * 31
}
