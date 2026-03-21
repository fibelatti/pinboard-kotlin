package com.fibelatti.pinboard.core.network

import java.net.InetAddress
import java.net.Socket
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * An [SSLSocketFactory] that delegates to a fresh [SSLContext] on each [reset] call.
 *
 * A new [SSLContext] starts with an empty session cache, which prevents TLS session resumption and
 * ensures the next connection performs a full handshake, consulting the [keyManager] for the
 * current client certificate alias.
 */
@Singleton
class LinkdingSSLSocketFactory @Inject constructor(
    private val keyManager: LinkdingClientCertKeyManager,
) : SSLSocketFactory() {

    val trustManager: X509TrustManager = run {
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)
        tmf.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
            ?: error("No X509TrustManager found.")
    }

    @Volatile
    private var delegate: SSLSocketFactory = newDelegate()

    private fun newDelegate(): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf(keyManager), arrayOf(trustManager), null)
        return sslContext.socketFactory
    }

    /**
     * Replaces the [delegate] with a fresh [SSLContext], clearing any cached TLS sessions.
     * Call this whenever the client certificate alias changes.
     */
    fun reset() {
        delegate = newDelegate()
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return delegate.createSocket(s, host, port, autoClose)
    }

    override fun createSocket(host: String, port: Int): Socket {
        return delegate.createSocket(host, port)
    }

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return delegate.createSocket(host, port, localHost, localPort)
    }

    override fun createSocket(host: InetAddress, port: Int): Socket {
        return delegate.createSocket(host, port)
    }

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return delegate.createSocket(address, port, localAddress, localPort)
    }
}
