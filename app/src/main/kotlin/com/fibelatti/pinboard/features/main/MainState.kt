package com.fibelatti.pinboard.features.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.Content
import kotlin.reflect.KClass

data class MainState(
    val title: TitleComponent = TitleComponent.Gone,
    val subtitle: TitleComponent = TitleComponent.Gone,
    val navigation: NavigationComponent = NavigationComponent.Gone,
    val actionButton: ActionButtonComponent = ActionButtonComponent.Gone,
    val bottomAppBar: BottomAppBarComponent = BottomAppBarComponent.Gone,
    val floatingActionButton: FabComponent = FabComponent.Gone,
    val sidePanelAppBar: SidePanelAppBarComponent = SidePanelAppBarComponent.Gone,
    val scrollDirection: ScrollDirection = ScrollDirection.IDLE,
) {

    sealed class TitleComponent {

        data object Gone : TitleComponent()
        data class Visible(val label: String) : TitleComponent()
    }

    sealed class NavigationComponent {

        data object Gone : NavigationComponent()

        data class Visible(@DrawableRes val icon: Int = R.drawable.ic_back_arrow) : NavigationComponent()
    }

    sealed class ActionButtonComponent {

        abstract val contentType: ContentType

        data object Gone : ActionButtonComponent() {

            override val contentType: ContentType = Content::class
        }

        data class Visible(
            override val contentType: ContentType,
            val label: String,
            val data: Any? = null,
        ) : ActionButtonComponent()
    }

    sealed class BottomAppBarComponent {

        abstract val contentType: ContentType

        data object Gone : BottomAppBarComponent() {

            override val contentType: ContentType = Content::class
        }

        data class Visible(
            override val contentType: ContentType,
            val menuItems: List<MenuItemComponent>,
            @DrawableRes val navigationIcon: Int? = null,
            val data: Any? = null,
        ) : BottomAppBarComponent()
    }

    sealed class MenuItemComponent(
        @StringRes val name: Int,
        @DrawableRes val icon: Int?,
    ) {

        data object ClearSearch : MenuItemComponent(
            name = R.string.menu_search_clear,
            icon = R.drawable.ic_clear_filter,
        )

        data object SaveSearch : MenuItemComponent(
            name = R.string.menu_search_save,
            icon = R.drawable.ic_save,
        )

        data object CloseSidePanel : MenuItemComponent(
            name = R.string.menu_side_panel_dismiss,
            icon = R.drawable.ic_close,
        )

        data object DeleteBookmark : MenuItemComponent(
            name = R.string.menu_link_delete,
            icon = R.drawable.ic_delete,
        )

        data object EditBookmark : MenuItemComponent(
            name = R.string.menu_link_edit,
            icon = R.drawable.ic_edit,
        )

        data object OpenInBrowser : MenuItemComponent(
            name = R.string.menu_link_open_in_browser,
            icon = R.drawable.ic_browser,
        )

        data object SaveBookmark : MenuItemComponent(
            name = R.string.menu_link_save,
            icon = R.drawable.ic_save,
        )

        data object SearchBookmarks : MenuItemComponent(
            name = R.string.menu_main_search,
            icon = R.drawable.ic_search,
        )

        data object ShareBookmark : MenuItemComponent(
            name = R.string.menu_link_share,
            icon = R.drawable.ic_share,
        )

        data object SortBookmarks : MenuItemComponent(
            name = R.string.menu_main_sorting,
            icon = R.drawable.ic_sort,
        )

        data object SyncBookmarks : MenuItemComponent(
            name = R.string.menu_main_sync,
            icon = R.drawable.ic_sync,
        )
    }

    sealed class FabComponent {

        abstract val contentType: ContentType

        data object Gone : FabComponent() {

            override val contentType: ContentType = Content::class
        }

        data class Visible(
            override val contentType: ContentType,
            @DrawableRes val icon: Int,
            val data: Any? = null,
        ) : FabComponent()
    }

    sealed class SidePanelAppBarComponent {

        abstract val contentType: ContentType

        data object Gone : SidePanelAppBarComponent() {

            override val contentType: ContentType = Content::class
        }

        data class Visible(
            override val contentType: ContentType,
            val menuItems: List<MenuItemComponent>,
            val data: Any? = null,
        ) : SidePanelAppBarComponent()
    }
}

typealias ContentType = KClass<out Content>
