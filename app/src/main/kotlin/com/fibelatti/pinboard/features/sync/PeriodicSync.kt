package com.fibelatti.pinboard.features.sync

sealed class PeriodicSync(val hours: Long) {

    object Off : PeriodicSync(hours = -1)
    object Every6Hours : PeriodicSync(hours = 6)
    object Every12Hours : PeriodicSync(hours = 12)
    object Every24Hours : PeriodicSync(hours = 24)
}
