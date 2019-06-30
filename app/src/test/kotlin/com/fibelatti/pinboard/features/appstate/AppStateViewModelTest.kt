package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.archcomponents.extension.asLiveData
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

internal class AppStateViewModelTest : BaseViewModelTest() {

    private val mockAppStateRepository = mock<AppStateRepository>()

    private val appStateViewModel = AppStateViewModel(
        mockAppStateRepository
    )

    @Test
    fun `WHEN getContent is called THEN repository content should be returned`() {
        // GIVEN
        val mockContent = mock<Content>()

        given(mockAppStateRepository.getContent())
            .willReturn(mockContent.asLiveData())

        // THEN
        appStateViewModel.getContent().currentValueShouldBe(mockContent)
    }

    @Test
    fun `WHEN runAction is called THEN repository should runAction`() {
        // GIVEN
        val mockAction = mock<Action>()

        // WHEN
        appStateViewModel.runAction(mockAction)

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(mockAction) }
    }
}
