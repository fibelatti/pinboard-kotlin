package com.fibelatti.pinboard.core.persistence.database

import androidx.room.RoomRawQuery

/**
 * Creates a [RoomRawQuery] from [sql], binding each of [args] to the placeholder at the same position.
 *
 * Bindings are positional and 1-based, matching the order in which the query builders assemble both the statement and
 * its arguments.
 */
fun rawQuery(sql: String, args: List<Any>): RoomRawQuery = RoomRawQuery(sql = sql) { statement ->
    args.forEachIndexed { index, arg ->
        when (arg) {
            is String -> statement.bindText(index + 1, arg)
            is Int -> statement.bindLong(index + 1, arg.toLong())
            else -> error("Cannot bind $arg at index ${index + 1}. Supported types: Int, String.")
        }
    }
}
