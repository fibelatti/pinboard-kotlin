package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class ConnectivityInfoProviderTest {

    private val mockNetworkA = mockk<Network>()
    private val mockNetworkB = mockk<Network>()
    private val mockNetworkC = mockk<Network>()

    private val mockNetworkACapabilities = mockk<NetworkCapabilities>().also {
        every { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }
    private val mockNetworkBCapabilities = mockk<NetworkCapabilities>().also {
        every { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
    }

    private val mockConnectivityManager = mockk<ConnectivityManager>().also {
        every { it.getNetworkCapabilities(mockNetworkA) } returns mockNetworkACapabilities
        every { it.getNetworkCapabilities(mockNetworkB) } returns mockNetworkBCapabilities
        every { it.getNetworkCapabilities(mockNetworkC) } returns null
    }

    private val connectivityInfoProvider = ConnectivityInfoProvider(mockConnectivityManager)

    @Test
    fun `WHEN ConnectivityManager is null THEN isConnected should return false`() {
        assertThat(ConnectivityInfoProvider(connectivityManager = null).isConnected()).isFalse()
    }

    @Test
    fun `WHEN allNetworks returns empty THEN isConnected should return false`() {
        every { mockConnectivityManager.allNetworks } returns emptyArray()

        assertThat(connectivityInfoProvider.isConnected()).isFalse()
    }

    @Test
    fun `WHEN the network has no capabilities THEN isConnected should return false`() {
        every { mockConnectivityManager.allNetworks } returns arrayOf(mockNetworkC)

        assertThat(connectivityInfoProvider.isConnected()).isFalse()
    }

    @Test
    fun `WHEN the network has no internet capability THEN isConnected should return false`() {
        every { mockConnectivityManager.allNetworks } returns arrayOf(mockNetworkB, mockNetworkC)

        assertThat(connectivityInfoProvider.isConnected()).isFalse()
    }

    @Test
    fun `WHEN at least one network has internet capability THEN isConnected should return true`() {
        every { mockConnectivityManager.allNetworks } returns arrayOf(
            mockNetworkA,
            mockNetworkB,
            mockNetworkC
        )

        assertThat(connectivityInfoProvider.isConnected()).isTrue()
    }
}
