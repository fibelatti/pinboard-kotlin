package com.fibelatti.pinboard.core.android.customview

import com.fibelatti.core.android.recyclerview.PagingHandler
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class PagingHandlerTest {

    private val pagingHandler = PagingHandler()

    @Test
    fun `GIVEN pageSize is 0 WHEN shouldHandlePaging is called THEN false is returned`() {
        // GIVEN
        pagingHandler.setPageSize(0)
        pagingHandler.setMinDistanceToLastItem(50)

        // THEN
        assertThat(pagingHandler.shouldHandlePaging(itemCount = 100, firstVisibleItemPosition = 51))
            .isFalse()
    }

    @Test
    fun `GIVEN minDistanceToLastItem is 0 WHEN shouldHandlePaging is called THEN false is returned`() {
        // GIVEN
        pagingHandler.setPageSize(100)
        pagingHandler.setMinDistanceToLastItem(0)

        // THEN
        assertThat(pagingHandler.shouldHandlePaging(itemCount = 100, firstVisibleItemPosition = 51))
            .isFalse()
    }

    @Test
    fun `GIVEN itemCount is 0 WHEN shouldHandlePaging is called THEN false is returned`() {
        // GIVEN
        pagingHandler.setPageSize(100)
        pagingHandler.setMinDistanceToLastItem(50)

        // THEN
        assertThat(pagingHandler.shouldHandlePaging(itemCount = 0, firstVisibleItemPosition = 0))
            .isFalse()
    }

    @Test
    fun `GIVEN the mod of itemCount to the page size is not 0 WHEN shouldHandlePaging is called THEN false is returned`() {
        // GIVEN
        pagingHandler.setPageSize(100)
        pagingHandler.setMinDistanceToLastItem(50)

        // THEN
        assertThat(
            pagingHandler.shouldHandlePaging(
                itemCount = 106,
                firstVisibleItemPosition = 105,
            ),
        ).isFalse()
    }

    @Test
    fun `GIVEN the first visible item is still too far from the min distance WHEN shouldHandlePaging is called THEN false is returned`() {
        // GIVEN
        pagingHandler.setPageSize(100)
        pagingHandler.setMinDistanceToLastItem(50)

        // THEN
        assertThat(pagingHandler.shouldHandlePaging(itemCount = 100, firstVisibleItemPosition = 49))
            .isFalse()
    }

    @Test
    fun `GIVEN the next page is already being requested WHEN shouldHandlePaging is called THEN false is returned`() {
        // GIVEN
        pagingHandler.setPageSize(100)
        pagingHandler.setMinDistanceToLastItem(50)
        pagingHandler.setRequestingNextPage(true)

        // THEN
        assertThat(pagingHandler.shouldHandlePaging(itemCount = 100, firstVisibleItemPosition = 52))
            .isFalse()
    }

    @Test
    fun `GIVEN all the conditions are met WHEN shouldHandlePaging is called THEN true is returned`() {
        // GIVEN
        pagingHandler.setPageSize(100)
        pagingHandler.setMinDistanceToLastItem(50)

        // THEN
        assertThat(pagingHandler.shouldHandlePaging(itemCount = 100, firstVisibleItemPosition = 51))
            .isTrue()
    }
}
