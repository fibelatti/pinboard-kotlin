package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.core.di.mapkeys.ViewModelKey
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.splash.presentation.SplashFragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class NavigationModule {

    @Binds
    @IntoMap
    @ViewModelKey(NavigationViewModel::class)
    abstract fun navigationViewModel(navigationViewModel: NavigationViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentKey(SplashFragment::class)
    abstract fun splashFragment(splashFragment: SplashFragment): Fragment
}
