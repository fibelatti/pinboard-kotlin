package com.fibelatti.pinboard.core.android

import android.Manifest
import android.content.Context
import android.os.Build
import com.fibelatti.pinboard.core.extension.isPermissionGranted
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android 17 (API 37) and above block connections to local network addresses unless
 * [Manifest.permission.ACCESS_LOCAL_NETWORK] is granted. Blocked connections time out silently
 * instead of failing fast, so the permission has to be granted before a self-hosted instance can
 * be reached.
 *
 * The block applies to the address rather than the scheme, so https instances are affected just as
 * much as cleartext ones.
 */
@Singleton
class LocalNetworkAccessProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Returns true when [instanceUrl] points at an address in the local network that the app is not allowed
     * to reach yet, meaning the permission must be granted before connecting to it.
     */
    suspend fun isPermissionRequired(instanceUrl: String?): Boolean {
        if (context.isPermissionGranted(permission = PERMISSION, minSdk = Build.VERSION_CODES.CINNAMON_BUN)) {
            return false
        }

        val host: String = instanceUrl?.hostOrNull() ?: return false

        return isLocalNetworkHost(host)
    }

    private fun String.hostOrNull(): String? = runCatching {
        val trimmed: String = trim()
        // The user is free to type an address without a scheme, and URI requires one to detect the host.
        URI(if ("://" in trimmed) trimmed else "https://$trimmed").host
    }.getOrNull()

    private suspend fun isLocalNetworkHost(host: String): Boolean = withContext(Dispatchers.IO) {
        if (LOCAL_DOMAIN_SUFFIXES.any { host.endsWith(it, ignoreCase = true) }) return@withContext true

        runCatching { InetAddress.getAllByName(host) }.fold(
            onSuccess = { addresses -> addresses.any(InetAddress::isLocalNetworkAddress) },
            // Resolving a local hostname can itself be blocked by the same restriction, so assume the address
            // is local rather than leaving the user with a silent timeout and no way to recover.
            onFailure = { true },
        )
    }

    companion object {

        /**
         * [Manifest.permission.ACCESS_LOCAL_NETWORK] only exists from API 37, so referencing that field
         * directly would inline its value into builds running on older platforms. The permission name is a
         * stable platform constant, making it safe to declare here and use from any API level.
         */
        const val PERMISSION: String = "android.permission.ACCESS_LOCAL_NETWORK"

        private val LOCAL_DOMAIN_SUFFIXES: List<String> = listOf(".local", ".home.arpa", ".internal", ".lan")
    }
}

private fun InetAddress.isLocalNetworkAddress(): Boolean =
    isSiteLocalAddress || isLinkLocalAddress || isUniqueLocalIpv6()

/**
 * [InetAddress.isSiteLocalAddress] only covers the deprecated fec0::/10 range, so the range actually in use
 * for private IPv6 addresses has to be checked separately.
 */
private fun InetAddress.isUniqueLocalIpv6(): Boolean =
    this is Inet6Address && address.firstOrNull()?.toInt()?.and(0xFE) == 0xFC
