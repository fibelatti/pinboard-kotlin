package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.features.user.data.UserDataSource
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.features.user.presentation.AuthFragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthModule {

    @Binds
    abstract fun userRepository(userDataSource: UserDataSource): UserRepository

    @Binds
    @IntoMap
    @FragmentKey(AuthFragment::class)
    abstract fun authFragment(authFragment: AuthFragment): Fragment
}
