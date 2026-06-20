package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Done
import com.fibelatti.pinboard.core.android.icons.Share
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PopularPostsContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.posts.presentation.isFile
import javax.inject.Inject

class BookmarkDetailsReducer @Inject constructor(
    private val bookmarkListReducer: BookmarkListReducer,
    private val popularBookmarksReducer: PopularBookmarksReducer,
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val (post, menuItems) = when (val current = appState.content) {
            is PostDetailContent -> current.post to buildList {
                add(MainState.MenuItemComponent.DeleteBookmark)
                if (AppMode.LINKDING == appState.appMode) {
                    add(MainState.MenuItemComponent.ToggleArchived(isArchived = current.post.isArchived == true))
                }
                add(MainState.MenuItemComponent.EditBookmark)
                add(MainState.MenuItemComponent.OpenInBrowser)
            }

            is PopularPostDetailContent -> current.post to listOf(
                MainState.MenuItemComponent.SaveBookmark,
                MainState.MenuItemComponent.OpenInBrowser,
            )

            else -> return mainState
        }

        val actionButtonState = if (post.readLater == true && !post.isFile()) {
            MainState.ActionButtonComponent.Visible(
                contentType = PostDetailContent::class,
                icon = AppIcons.Done,
                label = resourceProvider.getString(R.string.hint_mark_as_read),
                data = post,
            )
        } else {
            MainState.ActionButtonComponent.Gone
        }

        return if (appState.sidePanelVisible) {
            val baseState = appState.content.find<PopularPostsContent>()
                ?.let { popularBookmarksReducer(mainState = mainState, appState = appState) }
                ?: bookmarkListReducer(mainState = mainState, appState = appState)

            baseState.copy(
                actionButton = actionButtonState,
                sidePanelAppBar = MainState.SidePanelAppBarComponent.Visible(
                    contentType = PostDetailContent::class,
                    menuItems = listOf(
                        MainState.MenuItemComponent.ShareBookmark,
                        *menuItems.toTypedArray(),
                        MainState.MenuItemComponent.CloseSidePanel,
                    ),
                    data = post,
                ),
            )
        } else {
            mainState.copy(
                title = MainState.TitleComponent.Gone,
                subtitle = MainState.TitleComponent.Gone,
                navigation = MainState.NavigationComponent.Visible(),
                actionButton = actionButtonState,
                bottomAppBar = MainState.BottomAppBarComponent.Visible(
                    contentType = PostDetailContent::class,
                    menuItems = menuItems,
                    navigationIcon = null,
                    data = post,
                ),
                floatingActionButton = MainState.FabComponent.Visible(
                    contentType = PostDetailContent::class,
                    icon = AppIcons.Share,
                    data = post,
                ),
            )
        }
    }
}
