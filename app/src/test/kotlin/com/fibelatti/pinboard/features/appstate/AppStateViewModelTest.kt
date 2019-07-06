package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.archcomponents.extension.asLiveData
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.archcomponents.test.extension.shouldNeverReceiveValues
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.allSealedSubclasses
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito

internal class AppStateViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mock<AppStateRepository>()

    private lateinit var appStateViewModel: AppStateViewModel

    @Test
    fun `WHEN getContent is called THEN repository content should be returned`() {
        // GIVEN
        val mockContent = mock<Content>()

        given(mockAppStateRepository.getContent())
            .willReturn(mockContent.asLiveData())

        appStateViewModel = AppStateViewModel(mockAppStateRepository)

        // THEN
        appStateViewModel.content.currentValueShouldBe(mockContent)
    }

    @Test
    fun `WHEN runAction is called THEN repository should runAction`() {
        // GIVEN
        val mockAction = mock<Action>()

        appStateViewModel = AppStateViewModel(mockAppStateRepository)

        // WHEN
        appStateViewModel.runAction(mockAction)

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(mockAction) }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class SpecificContentTypeTests {

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only post list should be emitted to postList`(content: Content) {
            // GIVEN
            given(mockAppStateRepository.getContent())
                .willReturn(content.asLiveData())

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            if (content is PostList) {
                appStateViewModel.postList.currentValueShouldBe(content)
            } else {
                appStateViewModel.postList.shouldNeverReceiveValues()
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only post detail should be emitted to postDetail`(content: Content) {
            // GIVEN
            given(mockAppStateRepository.getContent())
                .willReturn(content.asLiveData())

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            if (content is PostDetail) {
                appStateViewModel.postDetail.currentValueShouldBe(content)
            } else {
                appStateViewModel.postDetail.shouldNeverReceiveValues()
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only edit post view should be emitted to editPostView`(content: Content) {
            // GIVEN
            given(mockAppStateRepository.getContent())
                .willReturn(content.asLiveData())

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            if (content is EditPostView) {
                appStateViewModel.editPostView.currentValueShouldBe(content)
            } else {
                appStateViewModel.editPostView.shouldNeverReceiveValues()
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only search view should be emitted to searchView`(content: Content) {
            // GIVEN
            given(mockAppStateRepository.getContent())
                .willReturn(content.asLiveData())

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            if (content is SearchView) {
                appStateViewModel.searchView.currentValueShouldBe(content)
            } else {
                appStateViewModel.searchView.shouldNeverReceiveValues()
            }
        }

        @ParameterizedTest
        @MethodSource("testCases")
        fun `Only tag list should be emitted to tagList`(content: Content) {
            // GIVEN
            given(mockAppStateRepository.getContent())
                .willReturn(content.asLiveData())

            appStateViewModel = AppStateViewModel(mockAppStateRepository)

            // THEN
            if (content is TagList) {
                appStateViewModel.tagList.currentValueShouldBe(content)
            } else {
                appStateViewModel.tagList.shouldNeverReceiveValues()
            }
        }

        fun testCases(): List<Content> = mutableListOf<Content>().apply {
            Content::class.allSealedSubclasses
                .map { it.objectInstance ?: Mockito.mock(it.javaObjectType) }
                .forEach { add(it) }
        }
    }
}
