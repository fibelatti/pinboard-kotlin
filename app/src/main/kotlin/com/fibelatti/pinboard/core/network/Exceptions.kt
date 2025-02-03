package com.fibelatti.pinboard.core.network

class MissingAuthTokenException : IllegalStateException()

class ApiException(resultCode: String? = null) : RuntimeException(resultCode)

class InvalidRequestException : RuntimeException()
