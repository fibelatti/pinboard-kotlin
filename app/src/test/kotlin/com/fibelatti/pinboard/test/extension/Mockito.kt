package com.fibelatti.pinboard.test.extension

import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Creates an instance of [T] using its first constructor and all of its properties will be mocks.
 *
 * @return an instance of [T]
 */
fun <T : Any> KClass<T>.createMockedInstance(): T {
    // Get the first constructor
    val constructor = constructors.first()

    // Get all its arguments and create mocks or instances for them
    val arguments = constructor.parameters
        .map {
            val kClass = it.type.classifier as KClass<*>

            try {
                // First we try to mock
                Mockito.mock(kClass.javaObjectType)
            } catch (exception: MockitoException) {
                // Not something we can mock (String, Class), so just use createInstance instead
                kClass.createInstance()
            }
        }
        .toTypedArray()

    // Finally use the arguments to instantiate the class
    return constructor.call(*arguments)
}
