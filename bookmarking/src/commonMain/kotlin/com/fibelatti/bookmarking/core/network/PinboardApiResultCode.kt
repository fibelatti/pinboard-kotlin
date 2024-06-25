package com.fibelatti.bookmarking.core.network

internal enum class PinboardApiResultCode(val value: String) {
    DONE(value = "done"),
    MISSING_URL(value = "missing url"),
    ITEM_ALREADY_EXISTS(value = "item already exists"),
}
