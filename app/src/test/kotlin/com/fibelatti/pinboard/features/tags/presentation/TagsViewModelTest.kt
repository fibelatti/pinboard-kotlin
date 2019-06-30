package com.fibelatti.pinboard.features.tags.presentation

import com.fibelatti.core.archcomponents.test.extension.currentValueShouldBe
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Success
import com.fibelatti.core.test.extension.givenSuspend
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.safeAny
import com.fibelatti.core.test.extension.verifySuspend
import com.fibelatti.pinboard.BaseViewModelTest
import com.fibelatti.pinboard.MockDataProvider.mockTags
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.SetSearchTags
import com.fibelatti.pinboard.features.appstate.SetTags
import com.fibelatti.pinboard.features.tags.domain.usecase.GetAllTags
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never

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
        tagsViewModel.getAll(mock())

        // THEN
        tagsViewModel.error.currentValueShouldBe(error)
        verifySuspend(mockAppStateRepository, never()) { runAction(safeAny()) }
    }

    @Test
    fun `GIVEN source is MENU WHEN getAllTags succeeds THEN AppStateRepository should run SetTags`() {
        // GIVEN
        givenSuspend { mockGetAllTags() }
            .willReturn(Success(mockTags))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.MENU)

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(SetTags(mockTags)) }
    }

    @Test
    fun `GIVEN source is SEARCH WHEN getAllTags succeeds THEN AppStateRepository should run SetSearchTags`() {
        // GIVEN
        givenSuspend { mockGetAllTags() }
            .willReturn(Success(mockTags))

        // WHEN
        tagsViewModel.getAll(TagsViewModel.Source.SEARCH)

        // THEN
        verifySuspend(mockAppStateRepository) { runAction(SetSearchTags(mockTags)) }
    }
}
