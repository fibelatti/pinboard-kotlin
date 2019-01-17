package com.fibelatti.pinboard.core.di.modules

import androidx.lifecycle.ViewModel
import com.fibelatti.pinboard.core.di.modules.viewmodel.ViewModelKey
import com.fibelatti.pinboard.features.auth.presentation.AuthViewModel
import com.fibelatti.pinboard.features.common.data.UserDataSource
import com.fibelatti.pinboard.features.common.domain.UnauthorizedHandler
import com.fibelatti.pinboard.features.common.domain.UnauthorizedHandlerDelegate
import com.fibelatti.pinboard.features.common.domain.UserRepository
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
