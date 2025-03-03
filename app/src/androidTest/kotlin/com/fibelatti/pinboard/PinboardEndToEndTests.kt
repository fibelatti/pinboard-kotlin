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
import com.fibelatti.pinboard.features.MainComposeActivity
import com.fibelatti.pinboard.features.appstate.EditPostContent
import com.fibelatti.pinboard.features.appstate.PostListContent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class PinboardEndToEndTests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainComposeActivity>()

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

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
        val context = composeRule.activity

        with(composeRule) {
            // Assert
            onNodeWithText(context.getString(R.string.auth_title_pinboard)).assertIsDisplayed()
        }
    }

    @Test
    fun userCanLaunchAppReviewMode() {
        val context = composeRule.activity

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
        val context = composeRule.activity

        // Arrange
        PinboardMockServer.setResponses(
            PinboardMockServer.updateResponse(updateTimestamp = dateFormatter.nowAsDataFormat()),
            PinboardMockServer.allBookmarksResponse(isEmpty = false),
        )

        with(composeRule) {
            // Act
            onNodeWithText(context.getString(R.string.auth_token_hint))
                .performTextInput(PinboardMockServer.TestData.TOKEN)
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
        val context = composeRule.activity

        // Arrange
        PinboardMockServer.setResponses(
            PinboardMockServer.updateResponse(updateTimestamp = dateFormatter.nowAsDataFormat()),
            PinboardMockServer.allBookmarksResponse(isEmpty = true),
            PinboardMockServer.addBookmarkResponse(),
        )

        with(composeRule) {
            // Login
            onNodeWithText(context.getString(R.string.auth_token_hint))
                .performTextInput(PinboardMockServer.TestData.TOKEN)
            onNodeWithText(context.getString(R.string.auth_button)).performClick()
            waitUntilAtLeastOneExists(
                matcher = hasText(context.getString(R.string.posts_title_all)),
                timeoutMillis = DEFAULT_TIMEOUT,
            )

            // Navigate to add bookmark screen
            onNodeWithTag(testTag = "fab-${PostListContent::class.simpleName}").performClick()
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
            onNodeWithTag(testTag = "fab-${EditPostContent::class.simpleName}")
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
