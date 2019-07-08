package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag

// region Action
sealed class Action

// region NavigationAction
sealed class NavigationAction : Action()

object NavigateBack : NavigationAction()
sealed class ViewCategory : NavigationAction()
data class ViewPost(val post: Post) : NavigationAction()
object ViewSearch : NavigationAction()
object AddPost : NavigationAction()
object ViewTags : NavigationAction()

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

object Refresh : PostAction()
data class SetPosts(val posts: Pair<Int, List<Post>>?) : PostAction()
object GetNextPostPage : PostAction()
data class SetNextPostPage(val posts: Pair<Int, List<Post>>?) : PostAction()
object PostsDisplayed : PostAction()
object ToggleSorting : PostAction()
data class EditPost(val post: Post) : PostAction()
data class PostSaved(val post: Post) : PostAction()
object PostDeleted : PostAction()
// endregion

// region SearchAction
sealed class SearchAction : Action()

object RefreshSearchTags : SearchAction()
data class SetSearchTags(val tags: List<Tag>) : SearchAction()
data class AddSearchTag(val tag: Tag) : SearchAction()
data class RemoveSearchTag(val tag: Tag) : SearchAction()
data class Search(val term: String) : SearchAction()
object ClearSearch : SearchAction()
// endregion

// region TagAction
sealed class TagAction : Action()

object RefreshTags : TagAction()
data class SetTags(val tags: List<Tag>) : TagAction()
data class PostsForTag(val tag: Tag) : TagAction()
// endregion
// endregion
