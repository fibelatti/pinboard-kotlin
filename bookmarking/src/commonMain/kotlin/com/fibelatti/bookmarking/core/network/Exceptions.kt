package com.fibelatti.bookmarking.core.network

public class ApiException(resultCode: String? = null) : Throwable(message = resultCode)

public class InvalidRequestException : Throwable()
