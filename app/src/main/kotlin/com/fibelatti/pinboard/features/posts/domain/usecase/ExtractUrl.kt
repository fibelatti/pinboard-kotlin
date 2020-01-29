package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import javax.inject.Inject

class ExtractUrl @Inject constructor() : UseCaseWithParams<String, String>() {

    override suspend fun run(params: String): Result<String> {
        val schemes = ValidUrlScheme.ALL_SCHEMES.map { "$it://" }

        for (scheme in schemes) {
            val index = params.indexOf(scheme)
            if (index != -1) {
                return Success(params.substring(index))
            }
        }

        return Failure(InvalidUrlException())
    }
}
