package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.archcomponents.test.BaseViewModelTest
import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import org.junit.jupiter.api.Test

internal class TagsViewModelTest : BaseViewModelTest() {

    private val mockGetAllTags = mock<GetAllTags>()
    private val mockAppStateRepository = mock<AppStateRepository>()

    private val tagsViewModel = TagsViewModel(
        mockGetAllTags,
        mockAppStateRepository
    )

    @Test
    fun `WHEN getAllTags fails THEN error should receive a value`() {
        // GIVEN
        val error = Exception()
        givenSuspend { mockGetAllTags() }
            .willReturn(Failure(error))

        // WHEN
        tagsViewModel.getAll()

        // THEN
        tagsViewModel.error.currentValueShouldBe(error)
    }

    @Test
    fun `WHEN getAllTags succeeds THEN AppStateRepository should run SetSearchTags`() {
        // GIVEN
        givenSuspend { mockGetAllTags() }
            .willReturn(Success(mockTags))

        // WHEN
        tagsViewModel.getAll()

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(SetSearchTags(mockTags)) }
    }
}
