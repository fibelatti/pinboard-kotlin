package com.fibelatti.pinboard.core.extension

// TODO - Move to CoreLib
inline fun <T, reified CastedT> T.applyAs(block: CastedT.() -> Unit): T {
    if (this is CastedT) this.block()
    return this
}
