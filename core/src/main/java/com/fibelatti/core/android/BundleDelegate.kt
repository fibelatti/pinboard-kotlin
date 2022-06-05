package com.fibelatti.core.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> bundleDelegate(default: T? = null): ReadWriteProperty<Bundle, T> = BundleDelegate(default)

fun <T> intentExtras(default: T? = null): ReadWriteProperty<Intent, T> = bundleDelegate(default).map(
    postWrite = Intent::replaceExtras,
    mapper = Intent::ensureExtras
)

fun <T> activityIntent(default: T? = null): ReadWriteProperty<Activity, T> = intentExtras(default).map(
    postWrite = Activity::setIntent,
    mapper = Activity::getIntent
)

fun <T> fragmentArgs(): ReadWriteProperty<Fragment, T> = bundleDelegate<T>().map(mapper = Fragment::ensureArgs)

fun <T> Bundle.asDelegate(default: T? = null): ReadWriteProperty<Any?, T> = bundleDelegate(default).map(
    mapper = { this }
)

private val Intent.ensureExtras: Bundle get() = extras ?: Bundle().also(::putExtras)

private val Fragment.ensureArgs: Bundle get() = arguments ?: Bundle().also(::setArguments)

private class BundleDelegate<T>(
    private val default: T? = null,
) : ReadWriteProperty<Bundle, T> {

    @Suppress("UNCHECKED_CAST")
    override operator fun getValue(
        thisRef: Bundle,
        property: KProperty<*>,
    ): T = (thisRef.get(property.name) ?: default) as T

    override fun setValue(
        thisRef: Bundle,
        property: KProperty<*>,
        value: T,
    ) = thisRef.putAll(bundleOf(property.name to value))
}

private class MappedDelegate<In, Out, T>(
    private val source: ReadWriteProperty<In, T>,
    private val postWrite: ((Out, In) -> Unit)? = null,
    private val mapper: (Out) -> In,
) : ReadWriteProperty<Out, T> {

    override fun getValue(thisRef: Out, property: KProperty<*>): T = source.getValue(mapper(thisRef), property)

    override fun setValue(thisRef: Out, property: KProperty<*>, value: T) {
        val mapped = mapper(thisRef)
        source.setValue(mapped, property, value)
        postWrite?.invoke(thisRef, mapped)
    }
}

private fun <In, Out, T> ReadWriteProperty<In, T>.map(
    postWrite: ((Out, In) -> Unit)? = null,
    mapper: (Out) -> In,
): ReadWriteProperty<Out, T> = MappedDelegate(source = this, postWrite = postWrite, mapper = mapper)
