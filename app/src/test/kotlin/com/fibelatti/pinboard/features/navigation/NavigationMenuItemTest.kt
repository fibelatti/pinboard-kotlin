package com.fibelatti.pinboard.features.navigation

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.features.appstate.AccountSwitcherContent
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.Archived
import com.fibelatti.pinboard.features.appstate.ExternalContent
import com.fibelatti.pinboard.features.appstate.NoteDetailContent
import com.fibelatti.pinboard.features.appstate.NoteListContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Private
import com.fibelatti.pinboard.features.appstate.Public
import com.fibelatti.pinboard.features.appstate.Recent
import com.fibelatti.pinboard.features.appstate.SearchParameters
import com.fibelatti.pinboard.features.appstate.ShouldLoadFirstPage
import com.fibelatti.pinboard.features.appstate.Unread
import com.fibelatti.pinboard.features.appstate.Untagged
import com.fibelatti.pinboard.features.appstate.ViewAccountSwitcher
import com.fibelatti.pinboard.features.appstate.ViewNotes
import com.fibelatti.pinboard.features.appstate.ViewOfflineCopies
import com.fibelatti.pinboard.features.appstate.ViewPopular
import com.fibelatti.pinboard.features.appstate.ViewPreferences
import com.fibelatti.pinboard.features.appstate.ViewSavedFilters
import com.fibelatti.pinboard.features.appstate.ViewTags
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class NavigationMenuItemTest {

    private val postListContent = PostListContent(
        category = Recent,
        posts = null,
        showDescription = false,
        sortType = mockk(),
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
    )

    @Nested
    inner class NavigationMenuGroupsTests {

        @Test
        fun `WHEN appMode is PINBOARD THEN the Pinboard destinations are included`() {
            assertThat(navigationMenuGroups(AppMode.PINBOARD).destinations()).containsExactly(
                listOf(All, Recent, Public, Private, Unread, Untagged, ViewPopular),
                listOf(ViewOfflineCopies, ViewSavedFilters, ViewTags, ViewNotes),
                listOf(ViewPreferences, ViewAccountSwitcher),
                emptyList<Any>(),
            ).inOrder()
        }

        @Test
        fun `WHEN appMode is LINKDING THEN the Linkding destinations are included`() {
            assertThat(navigationMenuGroups(AppMode.LINKDING).destinations()).containsExactly(
                listOf(All, Recent, Public, Private, Unread, Untagged, Archived),
                listOf(ViewOfflineCopies, ViewSavedFilters, ViewTags),
                listOf(ViewPreferences, ViewAccountSwitcher),
                emptyList<Any>(),
            ).inOrder()
        }

        @Test
        fun `WHEN appMode is NO_API THEN only the local destinations are included`() {
            assertThat(navigationMenuGroups(AppMode.NO_API).destinations()).containsExactly(
                listOf(All, Recent, Unread, Untagged),
                listOf(ViewOfflineCopies, ViewSavedFilters, ViewTags),
                listOf(ViewPreferences, ViewAccountSwitcher),
                emptyList<Any>(),
            ).inOrder()
        }

        @ParameterizedTest
        @EnumSource(AppMode::class)
        fun `WHEN the groups are built THEN the actions that are not destinations are included`(appMode: AppMode) {
            val actions = navigationMenuGroups(appMode).map { group ->
                group.map { it.action }.filterNot { it is NavigationMenuAction.Navigate }
            }

            assertThat(actions).containsExactly(
                emptyList<Any>(),
                emptyList<Any>(),
                listOf(NavigationMenuAction.ExportBookmarks),
                listOf(
                    NavigationMenuAction.SendFeedback,
                    NavigationMenuAction.WriteReview,
                    NavigationMenuAction.ShareApp,
                    NavigationMenuAction.ViewPrivacyPolicy,
                ),
            ).inOrder()
        }

        @Test
        fun `WHEN appMode is PINBOARD THEN the primary items are the most used destinations`() {
            val primary = navigationMenuGroups(AppMode.PINBOARD).flatten()
                .filter { it.isPrimary }
                .map { (it.action as NavigationMenuAction.Navigate).action }

            assertThat(primary).containsExactly(
                All,
                Unread,
                ViewOfflineCopies,
                ViewSavedFilters,
                ViewTags,
                ViewNotes,
                ViewPreferences,
            ).inOrder()
        }

        private fun List<List<NavigationMenuItem>>.destinations() = map { group ->
            group.mapNotNull { (it.action as? NavigationMenuAction.Navigate)?.action }
        }
    }

    @Nested
    inner class NavigationMenuDestinationTests {

        @Test
        fun `WHEN content is PostListContent THEN its category is returned`() {
            assertThat(postListContent.navigationMenuDestination()).isEqualTo(Recent)
        }

        @Test
        fun `WHEN content is a destination THEN its action is returned`() {
            val content = AccountSwitcherContent(previousContent = postListContent)

            assertThat(content.navigationMenuDestination()).isEqualTo(ViewAccountSwitcher)
        }

        @Test
        fun `WHEN content is not a destination THEN the destination in its history is returned`() {
            val content = NoteDetailContent(
                id = "some-id",
                note = mockk(),
                previousContent = NoteListContent(
                    notes = emptyList(),
                    shouldLoad = false,
                    previousContent = postListContent,
                ),
            )

            assertThat(content.navigationMenuDestination()).isEqualTo(ViewNotes)
        }

        @Test
        fun `WHEN content is a bookmark detail THEN the category of the list is returned`() {
            val content = PostDetailContent(
                post = mockk(),
                previousContent = postListContent,
            )

            assertThat(content.navigationMenuDestination()).isEqualTo(Recent)
        }

        @Test
        fun `WHEN content has no history THEN null is returned`() {
            assertThat(ExternalContent.navigationMenuDestination()).isNull()
        }
    }
}
