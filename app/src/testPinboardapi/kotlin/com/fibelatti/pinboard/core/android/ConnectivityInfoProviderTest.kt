package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.fibelatti.core.test.extension.mock
import com.fibelatti.core.test.extension.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

internal class ConnectivityInfoProviderTest {

    private val mockNetworkA = mock<Network>()
    private val mockNetworkB = mock<Network>()
    private val mockNetworkC = mock<Network>()

    private val mockNetworkACapabilities = mock<NetworkCapabilities>().also {
        given(it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .willReturn(true)
    }
    private val mockNetworkBCapabilities = mock<NetworkCapabilities>().also {
        given(it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .willReturn(false)
    }

    private val mockConnectivityManager = mock<ConnectivityManager>().also {
        given(it.getNetworkCapabilities(mockNetworkA))
            .willReturn(mockNetworkACapabilities)
        given(it.getNetworkCapabilities(mockNetworkB))
            .willReturn(mockNetworkBCapabilities)
        given(it.getNetworkCapabilities(mockNetworkC))
            .willReturn(null)
    }

    private val connectivityInfoProvider = ConnectivityInfoProvider(mockConnectivityManager)

    @Test
    fun `WHEN ConnectivityManager is null THEN isConnected should return false`() {
        ConnectivityInfoProvider(connectivityManager = null).isConnected() shouldBe false
    }

    @Test
    fun `WHEN allNetworks returns empty THEN isConnected should return false`() {
        given(mockConnectivityManager.allNetworks)
            .willReturn(emptyArray())

        connectivityInfoProvider.isConnected() shouldBe false
    }

    @Test
    fun `WHEN the network has no capabilities THEN isConnected should return false`() {
        given(mockConnectivityManager.allNetworks)
            .willReturn(arrayOf(mockNetworkC))

        connectivityInfoProvider.isConnected() shouldBe false
    }

    @Test
    fun `WHEN the network has no internet capability THEN isConnected should return false`() {
        given(mockConnectivityManager.allNetworks)
            .willReturn(arrayOf(mockNetworkB, mockNetworkC))

        connectivityInfoProvider.isConnected() shouldBe false
    }

    @Test
    fun `WHEN at least one network has internet capability THEN isConnected should return true`() {
        given(mockConnectivityManager.allNetworks)
            .willReturn(arrayOf(mockNetworkA, mockNetworkB, mockNetworkC))

        connectivityInfoProvider.isConnected() shouldBe true
    }
}
