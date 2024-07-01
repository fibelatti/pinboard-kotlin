package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import javax.inject.Inject

class ValidateUrl @Inject constructor() : UseCaseWithParams<String, Result<String>> {

    override suspend operator fun invoke(params: String): Result<String> =
        if (validate(params)) Success(params) else Failure(InvalidUrlException())

    private fun validate(url: String): Boolean =
        url.substringBefore("://", "") in ValidUrlScheme.ALL_SCHEMES
}
