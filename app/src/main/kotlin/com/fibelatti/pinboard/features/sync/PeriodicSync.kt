package com.fibelatti.pinboard.features.sync

sealed class PeriodicSync(val hours: Long) {

    data object Off : PeriodicSync(hours = -1)
    data object Every6Hours : PeriodicSync(hours = 6)
    data object Every12Hours : PeriodicSync(hours = 12)
    data object Every24Hours : PeriodicSync(hours = 24)
}
