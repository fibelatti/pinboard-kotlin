package com.fibelatti.pinboard.core.network

import retrofit2.HttpException
import java.net.HttpURLConnection

class ApiException : Throwable()

class InvalidRequestException : Throwable()

fun Throwable.isUnauthorized(): Boolean =
    this is HttpException && code() == HttpURLConnection.HTTP_UNAUTHORIZED
