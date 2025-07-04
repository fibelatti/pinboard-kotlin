package com.fibelatti.pinboard.features.main.reducer

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateAddedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabetical
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabeticalReverse
import com.fibelatti.pinboard.features.appstate.Loaded
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewCategory
import com.fibelatti.pinboard.features.appstate.find
import com.fibelatti.pinboard.features.main.MainState
import com.fibelatti.pinboard.features.main.MainState.ActionButtonComponent
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject

class BookmarkListReducer @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val userRepository: UserRepository,
) : MainStateReducer {

    override fun invoke(mainState: MainState, appState: AppState): MainState {
        val content: PostListContent = appState.content.find<PostListContent>() ?: return mainState
        val currentServiceName = when (appState.appMode) {
            AppMode.PINBOARD -> R.string.pinboard
            AppMode.LINKDING -> R.string.linkding
            else -> null
        }
        val connectedServices: Set<AppMode> = userRepository.userCredentials.value.getConnectedServices()
        val hasMultipleAccounts: Boolean = connectedServices.size > 1

        return mainState.copy(
            title = MainState.TitleComponent.Visible(getCategoryTitle(content.category)),
            subtitle = if (content.posts == null && content.shouldLoad is Loaded) {
                MainState.TitleComponent.Gone
            } else {
                MainState.TitleComponent.Visible(
                    label = buildPostCountSubTitle(content.totalCount, content.sortType),
                )
            },
            navigation = MainState.NavigationComponent.Gone,
            actionButton = when {
                hasMultipleAccounts && currentServiceName != null -> {
                    ActionButtonComponent.Visible(
                        contentType = AccountSwitcherContent::class,
                        label = resourceProvider.getString(currentServiceName),
                        data = connectedServices.first { it != appState.appMode },
                    )
                }

                else -> ActionButtonComponent.Gone
            },
            bottomAppBar = MainState.BottomAppBarComponent.Visible(
                contentType = PostListContent::class,
                menuItems = buildList {
                    add(MainState.MenuItemComponent.SearchBookmarks)
                    add(MainState.MenuItemComponent.SortBookmarks)

                    if (!content.posts?.list.isNullOrEmpty()) {
                        add(MainState.MenuItemComponent.RandomBookmark)
                    }

                    if (content.category == All && content.canForceSync) {
                        add(MainState.MenuItemComponent.SyncBookmarks)
                    }
                },
                navigationIcon = R.drawable.ic_menu,
            ),
            floatingActionButton = MainState.FabComponent.Visible(
                contentType = PostListContent::class,
                icon = R.drawable.ic_pin,
            ),
        )
    }

    private fun getCategoryTitle(category: ViewCategory): String = with(resourceProvider) {
        return when (category) {
            All -> getString(R.string.posts_title_all)
            Recent -> getString(R.string.posts_title_recent)
            Public -> getString(R.string.posts_title_public)
            Private -> getString(R.string.posts_title_private)
            Unread -> getString(R.string.posts_title_unread)
            Untagged -> getString(R.string.posts_title_untagged)
        }
    }

    private fun buildPostCountSubTitle(count: Int, sortType: SortType): String = with(resourceProvider) {
        val countFormatArg = if (count % AppConfig.API_PAGE_SIZE == 0) "$count+" else "$count"
        val countString = getQuantityString(R.plurals.posts_quantity, count, countFormatArg)
        return getString(
            when (sortType) {
                is ByDateAddedNewestFirst -> R.string.posts_sorting_newest_first
                is ByDateAddedOldestFirst -> R.string.posts_sorting_oldest_first
                is ByDateModifiedNewestFirst -> R.string.posts_sorting_newest_first
                is ByDateModifiedOldestFirst -> R.string.posts_sorting_oldest_first
                is ByTitleAlphabetical -> R.string.posts_sorting_alphabetical
                is ByTitleAlphabeticalReverse -> R.string.posts_sorting_alphabetical_reverse
            },
            countString,
        )
    }
}
