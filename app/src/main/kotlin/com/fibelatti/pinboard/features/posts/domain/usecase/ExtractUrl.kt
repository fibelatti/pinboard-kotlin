package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.inject.Inject

class ExtractUrl @Inject constructor() : UseCaseWithParams<String, String>() {

    override suspend fun run(params: String): Result<String> {
        val schemes = ValidUrlScheme.ALL_SCHEMES.map { "$it://" }

        val index = schemes.mapNotNull { scheme -> params.indexOf(scheme).takeIf { it >= 0 } }.min()
            ?: return Failure(InvalidUrlException())

        return try {
            Success(URLDecoder.decode(params.substring(index), "UTF-8"))
        } catch (ignored: UnsupportedEncodingException) {
            Failure(InvalidUrlException())
        }
    }
}
