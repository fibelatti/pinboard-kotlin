package com.fibelatti.pinboard.core.extension

import com.fibelatti.pinboard.core.network.MissingAuthTokenException
import com.google.common.truth.Truth.assertThat
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.JsonConvertException
import io.mockk.mockk
import java.net.UnknownHostException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException
import kotlinx.io.IOException
import org.junit.jupiter.api.Test

internal class ThrowableTest {

    @Test
    fun `isServerException should return true for server exception types`() {
        assertThat(MissingAuthTokenException().isServerException()).isTrue()
        assertThat(IOException("Connection reset").isServerException()).isTrue()
        assertThat(JsonConvertException("Illegal input").isServerException()).isTrue()
        assertThat(ServerResponseException(mockk(relaxed = true), "").isServerException()).isTrue()
    }

    @Test
    fun `isServerException should return true for subtypes of server exception types`() {
        assertThat(UnknownHostException("api.example.com").isServerException()).isTrue()
        assertThat(untrustedCertificateException().isServerException()).isTrue()
    }

    @Test
    fun `isCertificateException should return true when the certificate chain is not trusted`() {
        assertThat(untrustedCertificateException().isCertificateException()).isTrue()
    }

    @Test
    fun `isCertificateException should return false for other handshake failures`() {
        assertThat(SSLHandshakeException("Connection reset").isCertificateException()).isFalse()
        assertThat(IOException("Connection reset").isCertificateException()).isFalse()
    }

    private fun untrustedCertificateException(): SSLHandshakeException = SSLHandshakeException(
        "Handshake failed",
    ).apply {
        initCause(CertificateException("Trust anchor for certification path not found."))
    }

    @Test
    fun `isServerException should return true when a server exception is the cause`() {
        val throwable = Exception("Wrapper", JsonConvertException("Illegal input"))

        assertThat(throwable.isServerException()).isTrue()
    }

    @Test
    fun `isServerException should return false for other exceptions`() {
        assertThat(IllegalStateException().isServerException()).isFalse()
        assertThat(ClientRequestException(mockk(relaxed = true), "").isServerException()).isFalse()
        assertThat(Exception("Wrapper", IllegalStateException()).isServerException()).isFalse()
    }
}
