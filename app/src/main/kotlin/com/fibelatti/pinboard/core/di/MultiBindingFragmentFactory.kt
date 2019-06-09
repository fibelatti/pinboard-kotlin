package com.fibelatti.pinboard.core.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Inject
import javax.inject.Provider

class MultiBindingFragmentFactory @Inject constructor(
    private val creators: Map<Class<out Fragment>, @JvmSuppressWildcards Provider<Fragment>>
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragmentClass = loadFragmentClass(classLoader, className)
        val creator = creators[fragmentClass]
            ?: throw IllegalArgumentException("Unknown fragment class $fragmentClass")

        try {
            return creator.get()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

@Suppress("UnsafeCallOnNullableType") // Safe to suppress since T : Fragment, which is not a primitive type
inline fun <reified T : Fragment> FragmentFactory.createInstance(): Fragment =
    instantiate(T::class.java.classLoader!!, T::class.java.name)
