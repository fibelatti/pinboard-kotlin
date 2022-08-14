package com.fibelatti.pinboard.core.di.mapkeys

import com.fibelatti.pinboard.features.appstate.Action
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class ActionHandlerKey(val value: KClass<out Action>)
