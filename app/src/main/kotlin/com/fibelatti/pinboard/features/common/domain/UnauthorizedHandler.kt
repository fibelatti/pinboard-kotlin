package com.fibelatti.pinboard.features.common.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.core.network.isUnauthorized
import javax.inject.Inject

interface UnauthorizedHandler {
    fun <Type> Result<Type>.handleUnauthorized(): Result<Type>
}

class UnauthorizedHandlerDelegate @Inject constructor(
    private val userRepository: UserRepository
) : UnauthorizedHandler {

    override fun <Type> Result<Type>.handleUnauthorized(): Result<Type> = apply {
        onFailure { if (it.isUnauthorized()) userRepository.forceLogout() }
    }
}
