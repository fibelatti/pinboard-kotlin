package com.fibelatti.pinboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
abstract class BaseViewModelTest {

    protected open val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun baseSetup() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
