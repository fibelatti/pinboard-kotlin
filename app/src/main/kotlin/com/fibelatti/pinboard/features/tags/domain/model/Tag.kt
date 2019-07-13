package com.fibelatti.pinboard.features.tags.domain.model

import androidx.annotation.Keep

@Keep
data class Tag(val name: String, val posts: Int = 0) {

    override fun equals(other: Any?): Boolean = name == (other as? Tag)?.name

    override fun hashCode(): Int = name.hashCode() * 31
}
