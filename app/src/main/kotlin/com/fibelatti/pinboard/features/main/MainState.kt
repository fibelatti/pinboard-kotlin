package com.fibelatti.pinboard.features.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.Archive
import com.fibelatti.pinboard.core.android.icons.BackArrow
import com.fibelatti.pinboard.core.android.icons.Browser
import com.fibelatti.pinboard.core.android.icons.ClearFilter
import com.fibelatti.pinboard.core.android.icons.Close
import com.fibelatti.pinboard.core.android.icons.Delete
import com.fibelatti.pinboard.core.android.icons.Edit
import com.fibelatti.pinboard.core.android.icons.Random
import com.fibelatti.pinboard.core.android.icons.Save
import com.fibelatti.pinboard.core.android.icons.Search
import com.fibelatti.pinboard.core.android.icons.Share
import com.fibelatti.pinboard.core.android.icons.Sort
import com.fibelatti.pinboard.core.android.icons.Sync
import com.fibelatti.pinboard.core.android.icons.Unarchive
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

        data class Visible(val icon: ImageVector = AppIcons.BackArrow) : NavigationComponent()
    }

    sealed class ActionButtonComponent {

        abstract val contentType: ContentType

        data object Gone : ActionButtonComponent() {

            override val contentType: ContentType = Content::class
        }

        data class Visible(
            override val contentType: ContentType,
            val icon: ImageVector?,
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
            val navigationIcon: ImageVector? = null,
            val data: Any? = null,
        ) : BottomAppBarComponent()
    }

    sealed class MenuItemComponent(
        @StringRes val name: Int,
        val icon: ImageVector?,
    ) {

        data object ClearSearch : MenuItemComponent(
            name = R.string.menu_search_clear,
            icon = AppIcons.ClearFilter,
        )

        data object SaveSearch : MenuItemComponent(
            name = R.string.menu_search_save,
            icon = AppIcons.Save,
        )

        data object CloseSidePanel : MenuItemComponent(
            name = R.string.menu_side_panel_dismiss,
            icon = AppIcons.Close,
        )

        data object DeleteBookmark : MenuItemComponent(
            name = R.string.menu_link_delete,
            icon = AppIcons.Delete,
        )

        data object EditBookmark : MenuItemComponent(
            name = R.string.menu_link_edit,
            icon = AppIcons.Edit,
        )

        class ToggleArchived(isArchived: Boolean) : MenuItemComponent(
            name = if (isArchived) R.string.menu_link_unarchive else R.string.menu_link_archive,
            icon = if (isArchived) AppIcons.Unarchive else AppIcons.Archive,
        )

        data object OpenInBrowser : MenuItemComponent(
            name = R.string.menu_link_open_in_browser,
            icon = AppIcons.Browser,
        )

        data object SaveBookmark : MenuItemComponent(
            name = R.string.menu_link_save,
            icon = AppIcons.Save,
        )

        data object SearchBookmarks : MenuItemComponent(
            name = R.string.menu_main_search,
            icon = AppIcons.Search,
        )

        data object ShareBookmark : MenuItemComponent(
            name = R.string.menu_link_share,
            icon = AppIcons.Share,
        )

        data object SortBookmarks : MenuItemComponent(
            name = R.string.menu_main_sorting,
            icon = AppIcons.Sort,
        )

        data object RandomBookmark : MenuItemComponent(
            name = R.string.menu_read_random,
            icon = AppIcons.Random,
        )

        data object SyncBookmarks : MenuItemComponent(
            name = R.string.menu_main_sync,
            icon = AppIcons.Sync,
        )
    }

    sealed class FabComponent {

        abstract val contentType: ContentType

        data object Gone : FabComponent() {

            override val contentType: ContentType = Content::class
        }

        data class Visible(
            override val contentType: ContentType,
            val icon: ImageVector,
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
