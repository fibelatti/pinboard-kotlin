package com.fibelatti.bookmarking.core.network

public enum class PinboardApiResultCode(public val value: String) {
    DONE(value = "done"),
    MISSING_URL(value = "missing url"),
    ITEM_ALREADY_EXISTS(value = "item already exists"),
}
