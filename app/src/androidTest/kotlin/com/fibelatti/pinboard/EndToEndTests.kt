package com.fibelatti.pinboard

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.fibelatti.core.extension.clear
import com.fibelatti.pinboard.core.persistence.getUserPreferences
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.MainActivity
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
            waitUntilAtLeastOneExists(hasText(context.getString(R.string.posts_title_all)))
            onNodeWithText(context.getString(R.string.posts_title_all)).assertIsDisplayed()

            waitUntilAtLeastOneExists(hasText("Google"))
            onNodeWithText("Google").assertIsDisplayed()
            onNodeWithText("Private").assertIsDisplayed()
            onNodeWithText("Read later").assertIsDisplayed()
            onNodeWithText("android").assertIsDisplayed()
            onNodeWithText("dev").assertIsDisplayed()
        }
    }
}
