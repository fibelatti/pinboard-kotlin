package com.fibelatti.bookmarking.features.posts.domain

public sealed class EditAfterSharing(public val value: String) {

    public data object BeforeSaving : EditAfterSharing("BEFORE_SAVING")
    public data object AfterSaving : EditAfterSharing("AFTER_SAVING")
}
