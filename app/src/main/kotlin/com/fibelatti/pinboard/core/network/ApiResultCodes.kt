package com.fibelatti.pinboard.core.network

enum class ApiResultCodes(val code: String) {
    DONE("done"),
    MISSING_URL("missing url"),
    ITEM_ALREADY_EXISTS("item already exists")
    ;
}
