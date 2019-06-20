package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.posts.domain.model.Post

// region Action
sealed class Action

// region NavigationAction
sealed class NavigationAction : Action()

object NavigateBack : NavigationAction()
sealed class ViewCategory : NavigationAction()
class ViewPost(val post: Post) : NavigationAction()
object ViewSearch : NavigationAction()
object AddPost : NavigationAction()

// region ViewCategory
object All : ViewCategory()
object Recent : ViewCategory()
object Public : ViewCategory()
object Private : ViewCategory()
object Unread : ViewCategory()
object Untagged : ViewCategory()
object Tags : ViewCategory()
class Tag(val tagName: String) : ViewCategory()
// endregion
// endregion

// region PostAction
sealed class PostAction : Action()

class SetPosts(val posts: List<Post>) : PostAction()
object ToggleSorting : PostAction()
// endregion

// region SearchAction
sealed class SearchAction : Action()

class AddSearchTag(val tag: String) : SearchAction()
class RemoveSearchTag(val tag: String) : SearchAction()
class Search(val term: String) : SearchAction()
object ClearSearch : SearchAction()
// endregion
// endregion
