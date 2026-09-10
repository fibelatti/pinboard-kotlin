package com.fibelatti.pinboard.core.extension

import com.fibelatti.pinboard.core.network.MissingAuthTokenException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentConverterException
import io.ktor.serialization.ContentConvertException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import kotlin.reflect.KClass
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.io.IOException

fun Throwable.isServerException(): Boolean {
    val serverTypes = listOf(
        MissingAuthTokenException::class,
        IOException::class,
        TimeoutCancellationException::class,
        ServerResponseException::class,
        RedirectResponseException::class,
        ContentConverterException::class,
        ContentConvertException::class,
    )

    return anyOf(serverTypes)
}

/**
 * Returns true when the server's certificate chain could not be validated against the device trust
 * store, which happens when a self-hosted instance uses a self-signed certificate or one issued by
 * a certificate authority that the device does not trust.
 */
fun Throwable.isCertificateException(): Boolean {
    val certificateTypes = listOf(
        CertificateException::class,
        CertPathValidatorException::class,
    )

    return anyOf(certificateTypes)
}

private fun Throwable.anyOf(types: List<KClass<out Throwable>>): Boolean {
    return types.any { type -> type.isInstance(this) || cause?.let(type::isInstance) == true }
}
