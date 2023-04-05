package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag

// region Action
sealed class Action

// region AuthAction
sealed class AuthAction : Action()

object UserLoggedIn : AuthAction()
object UserLoggedOut : AuthAction()
object UserUnauthorized : AuthAction()
// endregion AuthAction

// region NavigationAction
sealed class NavigationAction : Action()

object NavigateBack : NavigationAction()
sealed class ViewCategory : NavigationAction()
data class ViewPost(val post: Post) : NavigationAction()
object ViewSearch : NavigationAction()
object AddPost : NavigationAction()
object ViewTags : NavigationAction()
object ViewNotes : NavigationAction()
data class ViewNote(val id: String) : NavigationAction()
object ViewPopular : NavigationAction()
object ViewPreferences : NavigationAction()

// region ViewCategory
object All : ViewCategory()
object Recent : ViewCategory()
object Public : ViewCategory()
object Private : ViewCategory()
object Unread : ViewCategory()
object Untagged : ViewCategory()
// endregion
// endregion

// region PostAction
sealed class PostAction : Action()

data class Refresh(val force: Boolean = false) : PostAction()
data class SetPosts(val postListResult: PostListResult) : PostAction()
object GetNextPostPage : PostAction()
data class SetNextPostPage(val postListResult: PostListResult) : PostAction()
object PostsDisplayed : PostAction()
object ToggleSorting : PostAction()
data class EditPost(val post: Post) : PostAction()
data class EditPostFromShare(val post: Post) : PostAction()
data class PostSaved(val post: Post) : PostAction()
object PostDeleted : PostAction()
// endregion

// region SearchAction
sealed class SearchAction : Action()

object RefreshSearchTags : SearchAction()
data class SetTerm(val term: String) : SearchAction()
data class SetSearchTags(val tags: List<Tag>) : SearchAction()
data class AddSearchTag(val tag: Tag) : SearchAction()
data class RemoveSearchTag(val tag: Tag) : SearchAction()
object Search : SearchAction()
object ClearSearch : SearchAction()
// endregion

// region TagAction
sealed class TagAction : Action()

object RefreshTags : TagAction()
data class SetTags(val tags: List<Tag>, val shouldReloadPosts: Boolean = false) : TagAction()
data class PostsForTag(val tag: Tag) : TagAction()
// endregion

// region NoteAction
sealed class NoteAction : Action()

object RefreshNotes : NoteAction()
data class SetNotes(val notes: List<Note>) : NoteAction()
data class SetNote(val note: Note) : NoteAction()
// endregion

// region PopularAction
sealed class PopularAction : Action()

object RefreshPopular : PopularAction()
data class SetPopularPosts(val posts: List<Post>) : PopularAction()
// endregion
// endregion
