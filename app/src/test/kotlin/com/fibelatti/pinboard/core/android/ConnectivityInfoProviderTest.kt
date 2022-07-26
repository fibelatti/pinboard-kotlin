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

    private val mockNetworkACapabilities = mockk<NetworkCapabilities> {
        every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }
    private val mockNetworkBCapabilities = mockk<NetworkCapabilities> {
        every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
    }

    private val mockConnectivityManager = mockk<ConnectivityManager> {
        every { getNetworkCapabilities(mockNetworkA) } returns mockNetworkACapabilities
        every { getNetworkCapabilities(mockNetworkB) } returns mockNetworkBCapabilities
        every { getNetworkCapabilities(mockNetworkC) } returns null
        every { getNetworkCapabilities(null) } returns null
    }

    private val connectivityInfoProvider = ConnectivityInfoProvider(
        connectivityManager = mockConnectivityManager,
        mainVariant = true,
    )

    @Test
    fun `WHEN ConnectivityManager is null THEN isConnected should return false`() {
        assertThat(ConnectivityInfoProvider(connectivityManager = null, mainVariant = true).isConnected()).isFalse()
    }

    @Test
    fun `WHEN activeNetwork returns null THEN isConnected should return false`() {
        every { mockConnectivityManager.activeNetwork } returns null

        assertThat(connectivityInfoProvider.isConnected()).isFalse()
    }

    @Test
    fun `WHEN the network has no capabilities THEN isConnected should return false`() {
        every { mockConnectivityManager.activeNetwork } returns mockNetworkC

        assertThat(connectivityInfoProvider.isConnected()).isFalse()
    }

    @Test
    fun `WHEN the network has no internet capability THEN isConnected should return false`() {
        every { mockConnectivityManager.activeNetwork } returns mockNetworkB

        assertThat(connectivityInfoProvider.isConnected()).isFalse()
    }

    @Test
    fun `WHEN the network has internet capability THEN isConnected should return true`() {
        every { mockConnectivityManager.activeNetwork } returns mockNetworkA

        assertThat(connectivityInfoProvider.isConnected()).isTrue()
    }
}
