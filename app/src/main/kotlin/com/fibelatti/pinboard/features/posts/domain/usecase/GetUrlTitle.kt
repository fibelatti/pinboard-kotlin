package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class GetUrlTitle @Inject constructor() : UseCaseWithParams<String, String>() {

    override suspend fun run(params: String): Result<String> = withContext(Dispatchers.IO) {
        Success(catching { Jsoup.connect(params).get().title() }.getOrDefault(params))
    }
}
