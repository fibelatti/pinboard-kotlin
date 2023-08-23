package com.fibelatti.pinboard

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.fibelatti.core.extension.clear
import com.fibelatti.pinboard.core.persistence.getUserPreferences
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.MainActivity
import com.fibelatti.pinboard.features.posts.presentation.EditPostFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class EndToEndTests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var dateFormatter: DateFormatter

    private val context get() = composeRule.activity

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        MockServer.instance.shutdown()
        context.getUserPreferences().clear()
    }

    @Test
    fun loginScreenIsVisibleWhenFirstLaunchingTheApp() {
        with(composeRule) {
            // Assert
            onNodeWithText(context.getString(R.string.auth_title)).assertIsDisplayed()
        }
    }

    @Test
    fun userCanLaunchAppReviewMode() {
        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_token_hint)).performTextInput("app_review_mode")
            onNodeWithText(context.getString(R.string.auth_button)).performClick()

            // Assert
            waitUntilAtLeastOneExists(hasText(context.getString(R.string.posts_title_all)))
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()
            onNodeWithText(context.getString(R.string.posts_empty_title)).assertIsDisplayed()
        }
    }

    @Test
    fun userCanLoginAndFetchBookmarks() {
        // Arrange
        MockServer.loginResponses(updateTimestamp = dateFormatter.nowAsTzFormat())

        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_token_hint)).performTextInput(MockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button)).performClick()

            // Assert
            waitUntilAtLeastOneExists(
                matcher = hasText(context.getString(R.string.posts_title_all)),
                timeoutMillis = 2_000L,
            )
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()

            waitUntilAtLeastOneExists(matcher = hasText("Google"), timeoutMillis = 2_000L)
            onNodeWithText("Google").assertIsDisplayed()
            onNodeWithText("Private").assertIsDisplayed()
            onNodeWithText("Read later").assertIsDisplayed()
            onNodeWithText("android").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }

    @Test
    fun userCanLoginAndAddBookmarks() {
        // Arrange
        MockServer.addBookmarkResponses(updateTimestamp = dateFormatter.nowAsTzFormat())

        with(composeRule) {
            // Login
            onNodeWithText(context.getString(R.string.auth_token_hint)).performTextInput(MockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button)).performClick()
            waitUntilAtLeastOneExists(hasText(context.getString(R.string.posts_title_all)))

            // Navigate to add bookmark screen
            onNodeWithTag(testTag = "fab-${PostListFragment.ACTION_ID}").performClick()
            waitUntilAtLeastOneExists(hasText(context.getString(R.string.posts_add_title)))

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
                .performTextInput("android")
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
                timeoutMillis = 2_000L,
            )
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()

            waitUntilAtLeastOneExists(matcher = hasText("Google"), timeoutMillis = 2_000L)
            onNodeWithText("Google").assertIsDisplayed()
            onNodeWithText("Private").assertIsDisplayed()
            onNodeWithText("Read later").assertIsDisplayed()
            onNodeWithText("android").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }
}
