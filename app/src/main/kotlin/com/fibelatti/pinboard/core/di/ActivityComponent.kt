package com.fibelatti.pinboard.core.di

import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import com.fibelatti.pinboard.core.di.modules.ActivityModule
import com.fibelatti.pinboard.core.di.modules.FeatureModule
import com.fibelatti.pinboard.features.FeatureFragments
import com.fibelatti.pinboard.features.InAppUpdateManager
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Scope

@Subcomponent(
    modules = [
        ActivityModule::class,
        FeatureModule::class
    ]
)
@ActivityScope
interface ActivityComponent : ViewModelProvider {

    fun fragmentFactory(): FragmentFactory
    fun featureFragments(): FeatureFragments
    fun inAppUpdateManager(): InAppUpdateManager

    @Subcomponent.Factory
    interface Factory {

        fun create(@BindsInstance activity: FragmentActivity): ActivityComponent
    }
}

/**
 * Identifies a type that the injector only instantiates once. Not inherited.
 *
 * @see javax.inject.Scope @Scope
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope
