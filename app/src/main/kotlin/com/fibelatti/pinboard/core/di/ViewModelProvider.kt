package com.fibelatti.pinboard.core.di

import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.notes.presentation.NoteDetailsViewModel
import com.fibelatti.pinboard.features.notes.presentation.NoteListViewModel
import com.fibelatti.pinboard.features.posts.presentation.EditPostViewModel
import com.fibelatti.pinboard.features.posts.presentation.PopularPostsViewModel
import com.fibelatti.pinboard.features.posts.presentation.PostDetailViewModel
import com.fibelatti.pinboard.features.posts.presentation.PostListViewModel
import com.fibelatti.pinboard.features.share.ShareReceiverViewModel
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.fibelatti.pinboard.features.user.presentation.UserPreferencesViewModel

interface ViewModelProvider {

    fun appStateViewModel(): AppStateViewModel
    fun authViewModel(): AuthViewModel
    fun editPostViewModel(): EditPostViewModel
    fun noteDetailsViewModel(): NoteDetailsViewModel
    fun noteListViewModel(): NoteListViewModel
    fun popularPostsViewModel(): PopularPostsViewModel
    fun postDetailsViewModel(): PostDetailViewModel
    fun postListViewModel(): PostListViewModel
    fun shareReceiverViewModel(): ShareReceiverViewModel
    fun tagsViewModel(): TagsViewModel
    fun userPreferencesViewModel(): UserPreferencesViewModel
}
