package com.fibelatti.pinboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import io.mockk.spyk
import io.mockk.verify

/**
 * An [Observer] only starts to receive data once it becomes active. To test that a given [LiveData]
 * has received multiple values it is necessary to start observing it before calling any methods that
 * would cause values to be emitted.
 *
 * This method will call [LiveData.observeForever] in the receiver with a [ListObserver], which can
 * later be used to assert the values received.
 *
 * @return a [ListObserver]
 */
fun <T> LiveData<T>.prepareToReceiveMany(): ListObserver<T> {
    val observer = ListObserver<T>()
    observeForever(observer)

    return observer
}

/**
 * Asserts that the receiver type received the same values as the [observer].
 */
fun <T> LiveData<T>.shouldHaveReceived(observer: ListObserver<T>, vararg expectedValues: T) {
    assertThat(expectedValues.toList()).isEqualTo(observer.getReceivedValues())
    removeObserver(observer)
}

/**
 * Observer class to be used in conjunction with [prepareToReceiveMany] and [shouldHaveReceived].
 *
 * It adds every value received by the observed [LiveData] to a list.
 */
class ListObserver<T> : Observer<T> {
    private val receivedValues: MutableList<T> = mutableListOf()

    override fun onChanged(value: T) {
        receivedValues += value
    }

    fun getReceivedValues(): List<T> = receivedValues
}

/**
 * Shorthand to assert that target [LiveData] never received any events.
 */
inline fun <reified T> LiveData<T>.shouldNeverReceiveValues() {
    val observer = spyk(Observer<T> {})
    observeForever(observer)
    verify(exactly = 0) { observer.onChanged(any()) }
    removeObserver(observer)
}
