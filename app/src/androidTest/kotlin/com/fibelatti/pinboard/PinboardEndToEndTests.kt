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
class PinboardEndToEndTests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val context get() = composeRule.activity

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        PinboardMockServer.instance.shutdown()
        sharedPreferences.clear()
    }

    @Test
    fun loginScreenIsVisibleWhenFirstLaunchingTheApp() {
        with(composeRule) {
            // Assert
            onNodeWithText(context.getString(R.string.auth_title_pinboard)).assertIsDisplayed()
        }
    }

    @Test
    fun userCanLaunchAppReviewMode() {
        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_token_hint)).performTextInput("app_review_mode")
            onNodeWithText(context.getString(R.string.auth_button)).performClick()

            // Assert
            waitUntilAtLeastOneExists(
                matcher = hasText(context.getString(R.string.posts_title_all)),
                timeoutMillis = DEFAULT_TIMEOUT,
            )
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()
            onNodeWithText(context.getString(R.string.posts_empty_title)).assertIsDisplayed()
        }
    }

    @Test
    fun userCanLoginAndFetchBookmarks() {
        // Arrange
        PinboardMockServer.setResponses(
            PinboardMockServer.updateResponse(updateTimestamp = dateFormatter.nowAsTzFormat()),
            PinboardMockServer.allBookmarksResponse(isEmpty = false),
        )

        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_token_hint)).performTextInput(PinboardMockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button)).performClick()

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
            onNodeWithText("android").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }

    @Test
    fun userCanLoginAndAddBookmarks() {
        // Arrange
        PinboardMockServer.setResponses(
            PinboardMockServer.updateResponse(updateTimestamp = dateFormatter.nowAsTzFormat()),
            PinboardMockServer.allBookmarksResponse(isEmpty = true),
            PinboardMockServer.addBookmarkResponse(),
        )

        with(composeRule) {
            // Login
            onNodeWithText(context.getString(R.string.auth_token_hint)).performTextInput(PinboardMockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button)).performClick()
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
            onNodeWithText("android").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }

    private companion object {

        const val DEFAULT_TIMEOUT = 3_000L
    }
}
