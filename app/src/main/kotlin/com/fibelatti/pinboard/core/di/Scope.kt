package com.fibelatti.pinboard.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Scope(val appDispatcher: AppDispatchers)

enum class AppDispatchers {
    DEFAULT,
    IO,
}
