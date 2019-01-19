package com.fibelatti.pinboard.core.di.modules

import androidx.lifecycle.ViewModel
import com.fibelatti.pinboard.core.di.ViewModelKey
import com.fibelatti.pinboard.features.user.presentation.AuthViewModel
import com.fibelatti.pinboard.features.user.data.UserDataSource
import com.fibelatti.pinboard.features.user.domain.UnauthorizedHandler
import com.fibelatti.pinboard.features.user.domain.UnauthorizedHandlerDelegate
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthModule {

    @Binds
    abstract fun userRepository(userDataSource: UserDataSource): UserRepository

    @Binds
    abstract fun unauthorizedHandler(
        unauthorizedHandlerDelegate: UnauthorizedHandlerDelegate
    ): UnauthorizedHandler

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun authViewModel(authViewModel: AuthViewModel): ViewModel
}
