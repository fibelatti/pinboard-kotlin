package com.fibelatti.bookmarking.features.sync

public sealed class PeriodicSync(public val hours: Long) {

    public data object Off : PeriodicSync(hours = -1)
    public data object Every6Hours : PeriodicSync(hours = 6)
    public data object Every12Hours : PeriodicSync(hours = 12)
    public data object Every24Hours : PeriodicSync(hours = 24)
}
