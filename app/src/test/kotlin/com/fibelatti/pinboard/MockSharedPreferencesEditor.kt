package com.fibelatti.core.test

import android.content.SharedPreferences

/**
 * Handy class to test interactions with [SharedPreferences]. Methods don't do anything.
 *
 * All methods that return [SharedPreferences.Editor] will return this instance of [MockSharedPreferencesEditor].
 * [SharedPreferences.Editor.commit] will always return true.
 */
open class MockSharedPreferencesEditor : SharedPreferences.Editor {

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun putString(key: String?, value: String?): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun remove(key: String?): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return this instance of [MockSharedPreferencesEditor]
     */
    override fun clear(): SharedPreferences.Editor = this

    /**
     * Stub method that does nothing.
     *
     * @return always true
     */
    override fun commit(): Boolean = true

    /**
     * Stub method that does nothing.
     */
    override fun apply() {
        // Intentionally empty
    }
}
