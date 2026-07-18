package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.ResultUseCaseWithParams
import javax.inject.Inject

class ValidateUrl @Inject constructor() : ResultUseCaseWithParams<String, String> {

    override suspend operator fun invoke(params: String): Result<String> {
        return if (validate(params)) {
            Result.success(params)
        } else {
            Result.failure(InvalidUrlException())
        }
    }

    private fun validate(url: String): Boolean {
        return url.substringBefore("://", "") in ValidUrlScheme.ALL_SCHEMES
    }
}
