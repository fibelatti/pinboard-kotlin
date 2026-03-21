package com.fibelatti.pinboard.core.network

import android.content.Context
import android.security.KeyChain
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager
import timber.log.Timber

@Singleton
class LinkdingClientCertKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userSharedPreferences: UserSharedPreferences,
) : X509ExtendedKeyManager() {

    override fun getClientAliases(keyType: String?, issuers: Array<Principal>?): Array<String>? {
        return null
    }

    override fun chooseClientAlias(keyType: Array<String>?, issuers: Array<Principal>?, socket: Socket?): String? {
        return userSharedPreferences.linkdingClientCertAlias.also { alias ->
            Timber.d("chooseClientAlias → $alias")
        }
    }

    override fun chooseEngineClientAlias(
        keyType: Array<String>?,
        issuers: Array<Principal>?,
        engine: SSLEngine?,
    ): String? {
        return userSharedPreferences.linkdingClientCertAlias.also { alias ->
            Timber.d("chooseEngineClientAlias → $alias")
        }
    }

    override fun getServerAliases(keyType: String?, issuers: Array<Principal>?): Array<String>? {
        return null
    }

    override fun chooseServerAlias(keyType: String?, issuers: Array<Principal>?, socket: Socket?): String? {
        return null
    }

    override fun chooseEngineServerAlias(keyType: String?, issuers: Array<Principal>?, engine: SSLEngine?): String? {
        return null
    }

    override fun getCertificateChain(alias: String?): Array<X509Certificate>? {
        return alias?.let {
            runCatching { KeyChain.getCertificateChain(context, alias) }
                .onFailure(Timber::e)
                .getOrNull()
                .also { chain -> Timber.d("getCertificateChain($alias) → ${chain?.size} certs") }
        }
    }

    override fun getPrivateKey(alias: String?): PrivateKey? {
        return alias?.let {
            runCatching { KeyChain.getPrivateKey(context, alias) }
                .onFailure(Timber::e)
                .getOrNull()
                .also { key -> Timber.d("getPrivateKey($alias) → ${if (key != null) "ok" else "null"}") }
        }
    }
}
