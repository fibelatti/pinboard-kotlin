package com.fibelatti.pinboard.core.persistence.database

private val ftsCompatibleRegex by lazy { Regex("^[a-zA-Z0-9\\s._\\-=#@&]*$") }

/**
 * Returns true if the [value] can be used as a FTS query on a table tokenized with
 * [androidx.room.FtsOptions.TOKENIZER_UNICODE61].
 */
fun isFtsCompatible(value: String): Boolean = value.matches(ftsCompatibleRegex)
