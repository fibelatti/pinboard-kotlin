package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

internal class ConnectivityInfoProviderTest {

    private val mockConnectivityManager = mock<ConnectivityManager>()

    private val connectivityInfoProvider = ConnectivityInfoProvider(mockConnectivityManager)

    @Test
    fun `WHEN ConnectivityManager is null THEN isConnected should return false`() {
        ConnectivityInfoProvider(connectivityManager = null).isConnected() shouldBe false
    }

    @Test
    fun `WHEN activeNetworkInfo is null THEN isConnected should return false`() {
        // GIVEN
        given(mockConnectivityManager.activeNetwork)
            .willReturn(null)

        // THEN
        connectivityInfoProvider.isConnected() shouldBe false
    }

    @Test
    fun `WHEN activityNetworkInfo is not connected THEN isConnected should return false`() {
        // GIVEN
        val mockActiveNetworkInfo = mock<NetworkInfo>()
        given(mockConnectivityManager.activeNetworkInfo)
            .willReturn(mockActiveNetworkInfo)
        given(mockActiveNetworkInfo.isConnected)
            .willReturn(false)

        // THEN
        connectivityInfoProvider.isConnected() shouldBe false
    }

    @Test
    fun `WHEN activityNetworkInfo is connected THEN isConnected should return true`() {
        // GIVEN
        val mockActiveNetworkInfo = mock<NetworkInfo>()
        given(mockConnectivityManager.activeNetworkInfo)
            .willReturn(mockActiveNetworkInfo)
        given(mockActiveNetworkInfo.isConnected)
            .willReturn(true)

        // THEN
        connectivityInfoProvider.isConnected() shouldBe true
    }
}
