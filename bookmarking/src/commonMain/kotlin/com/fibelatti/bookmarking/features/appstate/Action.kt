package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.features.filters.domain.model.SavedFilter
import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.bookmarking.features.tags.domain.model.Tag

// region Action
public sealed class Action

// region AuthAction
public sealed class AuthAction : Action()

public data object UserLoggedIn : AuthAction()
public data object UserLoggedOut : AuthAction()
public data object UserUnauthorized : AuthAction()
// endregion AuthAction

// region NavigationAction
public sealed class NavigationAction : Action()

public data object NavigateBack : NavigationAction()
public sealed class ViewCategory : NavigationAction()
public data class ViewPost(val post: Post) : NavigationAction()
public data object ViewSearch : NavigationAction()
public data object AddPost : NavigationAction()
public data object ViewTags : NavigationAction()
public data object ViewSavedFilters : NavigationAction()
public data object ViewNotes : NavigationAction()
public data class ViewNote(val id: String) : NavigationAction()
public data object ViewPopular : NavigationAction()
public data object ViewPreferences : NavigationAction()
// endregion NavigationAction

// region ViewCategory
public data object All : ViewCategory()
public data object Recent : ViewCategory()
public data object Public : ViewCategory()
public data object Private : ViewCategory()
public data object Unread : ViewCategory()
public data object Untagged : ViewCategory()
// endregion ViewCategory

// region PostAction
public sealed class PostAction : Action()

public data class Refresh(val force: Boolean = false) : PostAction()
public data class SetPosts(val postListResult: PostListResult) : PostAction()
public data object GetNextPostPage : PostAction()
public data class SetNextPostPage(val postListResult: PostListResult) : PostAction()
public data class SetSorting(val sortType: SortType) : PostAction()
public data class EditPost(val post: Post) : PostAction()
public data class EditPostFromShare(val post: Post) : PostAction()
public data class PostSaved(val post: Post) : PostAction()
public data object PostDeleted : PostAction()
// endregion PostAction

// region SearchAction
public sealed class SearchAction : Action()

public data object RefreshSearchTags : SearchAction()
public data class SetTerm(val term: String) : SearchAction()
public data class SetSearchTags(val tags: List<Tag>) : SearchAction()
public data class AddSearchTag(val tag: Tag) : SearchAction()
public data class RemoveSearchTag(val tag: Tag) : SearchAction()
public data object Search : SearchAction()
public data object ClearSearch : SearchAction()
public data class ViewSavedFilter(val savedFilter: SavedFilter) : SearchAction()
// endregion SearchAction

// region TagAction
public sealed class TagAction : Action()

public data object RefreshTags : TagAction()
public data class SetTags(val tags: List<Tag>, val shouldReloadPosts: Boolean = false) : TagAction()
public data class PostsForTag(val tag: Tag) : TagAction()
// endregion TagAction

// region NoteAction
public sealed class NoteAction : Action()

public data object RefreshNotes : NoteAction()
public data class SetNotes(val notes: List<Note>) : NoteAction()
public data class SetNote(val note: Note) : NoteAction()
// endregion NoteAction

// region PopularAction
public sealed class PopularAction : Action()

public data object RefreshPopular : PopularAction()
public data class SetPopularPosts(val posts: List<Post>) : PopularAction()
// endregion PopularAction
// endregion
