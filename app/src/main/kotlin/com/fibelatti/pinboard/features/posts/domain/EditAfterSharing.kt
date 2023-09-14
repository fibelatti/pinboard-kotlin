package com.fibelatti.pinboard.features.posts.domain

sealed class EditAfterSharing(val value: String) {

    data object BeforeSaving : EditAfterSharing("BEFORE_SAVING")
    data object AfterSaving : EditAfterSharing("AFTER_SAVING")
}
