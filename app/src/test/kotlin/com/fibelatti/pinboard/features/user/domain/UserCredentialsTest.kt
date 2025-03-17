package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.pinboard.core.AppMode
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserCredentialsTest {

    @Test
    fun `connectedServices - both services connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = "token",
            linkdingInstanceUrl = "https://example.com",
            linkdingAuthToken = "token",
        )

        val connectedServices = userCredentials.getConnectedServices()

        assertThat(connectedServices).containsExactly(AppMode.PINBOARD, AppMode.LINKDING)
    }

    @Test
    fun `connectedServices - only Pinboard connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = "token",
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        )

        val connectedServices = userCredentials.getConnectedServices()

        assertThat(connectedServices).containsExactly(AppMode.PINBOARD)
    }

    @Test
    fun `connectedServices - only Linkding connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = "https://example.com",
            linkdingAuthToken = "token",
        )

        val connectedServices = userCredentials.getConnectedServices()

        assertThat(connectedServices).containsExactly(AppMode.LINKDING)
    }

    @Test
    fun `connectedServices - no services connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        )

        val connectedServices = userCredentials.getConnectedServices()

        assertThat(connectedServices).isEmpty()
    }

    @Test
    fun `connectedServices - app review mode`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
            appReviewMode = true,
        )

        val connectedServices = userCredentials.getConnectedServices()

        assertThat(connectedServices).containsExactly(AppMode.NO_API)
    }

    @Test
    fun `hasAuthToken - true with both services connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = "token",
            linkdingInstanceUrl = "https://example.com",
            linkdingAuthToken = "token",
        )

        assertThat(userCredentials.hasAuthToken()).isTrue()
    }

    @Test
    fun `hasAuthToken - true with only Pinboard connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = "token",
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        )

        assertThat(userCredentials.hasAuthToken()).isTrue()
    }

    @Test
    fun `hasAuthToken - true with only Linkding connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = "https://example.com",
            linkdingAuthToken = "token",
        )

        assertThat(userCredentials.hasAuthToken()).isTrue()
    }

    @Test
    fun `hasAuthToken - false with no service connected`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        )

        assertThat(userCredentials.hasAuthToken()).isFalse()
    }

    @Test
    fun `hasAuthToken - true with app review mode`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
            appReviewMode = true,
        )

        assertThat(userCredentials.hasAuthToken()).isTrue()
    }

    @Test
    fun `pinboardUsername - valid username`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = "token:123",
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        )

        assertThat(userCredentials.getPinboardUsername()).isEqualTo("token")
    }

    @Test
    fun `pinboardUsername - null for null token`() {
        val userCredentials = UserCredentials(
            pinboardAuthToken = null,
            linkdingInstanceUrl = null,
            linkdingAuthToken = null,
        )

        assertThat(userCredentials.getPinboardUsername()).isNull()
    }
}
