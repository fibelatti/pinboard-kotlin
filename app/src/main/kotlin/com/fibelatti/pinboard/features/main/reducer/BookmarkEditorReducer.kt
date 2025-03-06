package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.features.appstate.AddPostContent
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.main.MainState
import javax.inject.Inject

class BookmarkEditorReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val post = when (appState.content) {
            is EditPostContent -> appState.content.post
            is AddPostContent -> null
            else -> return mainState
        }

        return mainState.copy(
            title = MainState.TitleComponent.Visible(resourceProvider.getString(R.string.posts_add_title)),
            subtitle = MainState.TitleComponent.Gone,
            navigation = MainState.NavigationComponent.Visible(icon = R.drawable.ic_close),
            bottomAppBar = MainState.BottomAppBarComponent.Visible(
                contentType = EditPostContent::class,
                menuItems = buildList {
                    if (post != null) {
                        add(MainState.MenuItemComponent.DeleteBookmark)
                        add(MainState.MenuItemComponent.OpenInBrowser)
                    }
                },
                navigationIcon = null,
                data = post,
            ),
            floatingActionButton = MainState.FabComponent.Visible(
                contentType = EditPostContent::class,
                icon = R.drawable.ic_done,
            ),
        )
    }
}
