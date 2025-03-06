package com.fibelatti.pinboard.core.di.mapkeys

import com.fibelatti.pinboard.features.main.ContentType
import dagger.MapKey

@MapKey
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class MainReducerKey(val value: ContentType)
