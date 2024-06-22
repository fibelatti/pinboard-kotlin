package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import org.koin.core.annotation.Factory

@Factory
class ValidateUrl() : UseCaseWithParams<String, String>() {

    override suspend fun run(params: String): Result<String> =
        if (validate(params)) Success(params) else Failure(InvalidUrlException())

    private fun validate(url: String): Boolean =
        url.substringBefore("://", "") in ValidUrlScheme.ALL_SCHEMES
}
