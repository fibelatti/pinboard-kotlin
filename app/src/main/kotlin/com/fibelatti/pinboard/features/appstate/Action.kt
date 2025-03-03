package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.filters.domain.model.SavedFilter
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class Action

// region App
sealed class AppAction : Action()

data class MultiPanelAvailabilityChanged(val available: Boolean) : AppAction()
data object Reset : AppAction()
// endregion App

// region AuthAction
sealed class AuthAction : Action()

data object UserLoggedIn : AuthAction()
data object UserLoggedOut : AuthAction()
data object UserUnauthorized : AuthAction()
// endregion AuthAction

// region NavigationAction
sealed class NavigationAction : Action()

data object NavigateBack : NavigationAction()
sealed class ViewCategory : NavigationAction()
data class ViewPost(val post: Post) : NavigationAction()
data object ViewSearch : NavigationAction()
data object AddPost : NavigationAction()
data object ViewTags : NavigationAction()
data object ViewSavedFilters : NavigationAction()
data object ViewNotes : NavigationAction()
data class ViewNote(val id: String) : NavigationAction()
data object ViewPopular : NavigationAction()
data object ViewPreferences : NavigationAction()

// region ViewCategory
data object All : ViewCategory()
data object Recent : ViewCategory()
data object Public : ViewCategory()
data object Private : ViewCategory()
data object Unread : ViewCategory()
data object Untagged : ViewCategory()
// endregion ViewCategory
// endregion NavigationAction

// region PostAction
sealed class PostAction : Action()

data class Refresh(val force: Boolean = false) : PostAction()
data class SetPosts(val postListResult: PostListResult) : PostAction()
data object GetNextPostPage : PostAction()
data class SetNextPostPage(val postListResult: PostListResult) : PostAction()
data class SetSorting(val sortType: SortType) : PostAction()
data class EditPost(val post: Post) : PostAction()
data class EditPostFromShare(val post: Post) : PostAction()
data class PostSaved(val post: Post) : PostAction()
data object PostDeleted : PostAction()
// endregion PostAction

// region SearchAction
sealed class SearchAction : Action()

data object RefreshSearchTags : SearchAction()
data class SetTerm(val term: String) : SearchAction()
data class SetSearchTags(val tags: List<Tag>) : SearchAction()
data class AddSearchTag(val tag: Tag) : SearchAction()
data class RemoveSearchTag(val tag: Tag) : SearchAction()
data object Search : SearchAction()
data object ClearSearch : SearchAction()
data class ViewSavedFilter(val savedFilter: SavedFilter) : SearchAction()
// endregion SearchAction

// region TagAction
sealed class TagAction : Action()

data object RefreshTags : TagAction()
data class SetTags(val tags: List<Tag>, val shouldReloadPosts: Boolean = false) : TagAction()
data class PostsForTag(val tag: Tag) : TagAction()
// endregion TagAction

// region NoteAction
sealed class NoteAction : Action()

data object RefreshNotes : NoteAction()
data class SetNotes(val notes: List<Note>) : NoteAction()
data class SetNote(val note: Note) : NoteAction()
// endregion NoteAction

// region PopularAction
sealed class PopularAction : Action()

data object RefreshPopular : PopularAction()
data class SetPopularPosts(val posts: Map<Post, Int>) : PopularAction()
// endregion PopularAction
