package com.fibelatti.pinboard.features.posts.domain

sealed class EditAfterSharing(val value: String) {

    object BeforeSaving : EditAfterSharing("BEFORE_SAVING")
    object AfterSaving : EditAfterSharing("AFTER_SAVING")
    object SkipEdit : EditAfterSharing("SKIP_EDIT")
}
