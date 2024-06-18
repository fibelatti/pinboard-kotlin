package com.fibelatti.pinboard.core.network

class ApiException(resultCode: String? = null) : Throwable(message = resultCode)

class InvalidRequestException : Throwable()
