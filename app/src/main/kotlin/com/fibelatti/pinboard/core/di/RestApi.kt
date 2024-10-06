package com.fibelatti.pinboard.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class RestApi(val restApi: RestApiProvider)

enum class RestApiProvider {
    BASE,
    PINBOARD,
    LINKDING,
}
