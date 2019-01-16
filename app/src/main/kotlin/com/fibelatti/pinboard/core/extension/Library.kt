package com.fibelatti.pinboard.core.extension

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.core.network.ApiException
import retrofit2.Response

fun <T : Any> Response<T>.toResult(): Result<T> =
    catching { if (isSuccessful) body() ?: throw ApiException() else throw ApiException() }
