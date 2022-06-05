package com.fibelatti.core.android

import androidx.annotation.StringRes

/**
 * Handy interface to abstract the Android Framework in ViewModels, Presenters, etc. With this it is possible to
 * retrieve string resources in scenarios which there's logic involved, improving testability.
 */
interface ResourceProvider {

    /**
     * Interface to retrieve a string resource.
     *
     * @param resId the resource id to get
     *
     * @return the resolved [String]
     */
    fun getString(@StringRes resId: Int): String

    /**
     * Interface to retrieve a string resource that has arguments.
     *
     * @param resId the resource id to get
     * @param formatArgs the arguments of the string resource
     *
     * @return the resolved [String]
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String

    /**
     * Interface to retrieve a json assert as a string.
     *
     * @param fileName the name of the json asset
     *
     * @return the json as a [String] if found, null otherwise
     */
    fun getJsonFromAssets(fileName: String): String?
}
