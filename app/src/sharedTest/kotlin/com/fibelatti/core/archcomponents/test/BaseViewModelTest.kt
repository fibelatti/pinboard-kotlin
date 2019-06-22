package com.fibelatti.core.archcomponents.test

import com.fibelatti.pinboard.InstantExecutorExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
abstract class BaseViewModelTest {

    @BeforeEach
    fun baseSetup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }
}
