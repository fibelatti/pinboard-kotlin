package com.fibelatti.pinboard

import android.content.SharedPreferences
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.fibelatti.core.android.extension.clear
import com.fibelatti.pinboard.features.MainActivity
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.inject

@OptIn(ExperimentalTestApi::class)
class LinkdingEndToEndTests : KoinTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val sharedPreferences: SharedPreferences by inject()

    private val context get() = composeRule.activity

    @After
    fun tearDown() {
        LinkdingMockServer.instance.shutdown()
        sharedPreferences.clear()
    }

    @Test
    fun loginScreenIsVisibleWhenFirstLaunchingTheApp() {
        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_switch_to_linkding)).performClick()

            // Assert
            onNodeWithText(context.getString(R.string.auth_title_linkding)).assertIsDisplayed()
        }
    }

    @Test
    fun userCanLoginAndFetchBookmarks() {
        // Arrange
        LinkdingMockServer.setResponses(
            LinkdingMockServer.allBookmarksResponse(isEmpty = false),
        )

        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_switch_to_linkding))
                .performClick()
            onNodeWithText(context.getString(R.string.auth_linkding_instance_url))
                .performTextInput(LinkdingMockServer.instance.url("/").toString())
            onNodeWithText(context.getString(R.string.auth_token_hint))
                .performTextInput(LinkdingMockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button))
                .performClick()

            // Assert
            waitUntilAtLeastOneExists(
                matcher = hasText(context.getString(R.string.posts_title_all)),
                timeoutMillis = DEFAULT_TIMEOUT,
            )
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()

            waitUntilAtLeastOneExists(
                matcher = hasText("Google"),
                timeoutMillis = DEFAULT_TIMEOUT,
            )
            onNodeWithText("Google").assertIsDisplayed()
            onNodeWithText("Private").assertIsDisplayed()
            onNodeWithText("Read later").assertIsDisplayed()
            onNodeWithText("kotlin").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }

    @Test
    fun userCanLoginAndAddBookmarks() {
        // Arrange
        LinkdingMockServer.setResponses(
            LinkdingMockServer.allBookmarksResponse(isEmpty = true),
            LinkdingMockServer.addBookmarkResponse(),
        )

        with(composeRule) {
            // Login
            onNodeWithText(context.getString(R.string.auth_switch_to_linkding))
                .performClick()
            onNodeWithText(context.getString(R.string.auth_linkding_instance_url))
                .performTextInput(LinkdingMockServer.instance.url("/").toString())
            onNodeWithText(context.getString(R.string.auth_token_hint))
                .performTextInput(LinkdingMockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button))
                .performClick()
            waitUntilAtLeastOneExists(
                matcher = hasText(context.getString(R.string.posts_title_all)),
                timeoutMillis = DEFAULT_TIMEOUT,
            )

            // Navigate to add bookmark screen
            onNodeWithTag(testTag = "fab-${PostListFragment.ACTION_ID}").performClick()
            waitUntilAtLeastOneExists(
                hasText(context.getString(R.string.posts_add_title)),
                timeoutMillis = DEFAULT_TIMEOUT,
            )

            // Enter bookmark details
            onNodeWithText(context.getString(R.string.posts_add_url))
                .performTextInput("https://www.google.com")
            onNodeWithText(context.getString(R.string.posts_add_url_title))
                .performTextInput("Google")
            onNodeWithText(context.getString(R.string.posts_add_url_description))
                .performTextInput("Instrumented test")
            onNodeWithTag(testTag = "setting-toggle-${context.getString(R.string.posts_add_private)}")
                .performClick()
            onNodeWithTag(testTag = "setting-toggle-${context.getString(R.string.posts_add_read_later)}")
                .performClick()
            onNodeWithText(context.getString(R.string.posts_add_tags))
                .performTextInput("kotlin")
            onNodeWithText(context.getString(R.string.posts_add_tags_add))
                .performClick()
            onNodeWithText(context.getString(R.string.posts_add_tags))
                .performTextInput("dev")
            onNodeWithText(context.getString(R.string.posts_add_tags_add))
                .performClick()

            // Save
            onNodeWithTag(testTag = "fab-${EditPostFragment.ACTION_ID}")
                .performClick()

            // Assert
            waitUntilAtLeastOneExists(
                matcher = hasText(context.getString(R.string.posts_title_all)),
                timeoutMillis = DEFAULT_TIMEOUT,
            )
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()

            waitUntilAtLeastOneExists(
                matcher = hasText("Google"),
                timeoutMillis = DEFAULT_TIMEOUT,
            )
            onNodeWithText("Google").assertIsDisplayed()
            onNodeWithText("Private").assertIsDisplayed()
            onNodeWithText("Read later").assertIsDisplayed()
            onNodeWithText("kotlin").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }

    private companion object {

        const val DEFAULT_TIMEOUT = 3_000L
    }
}
