package com.fibelatti.pinboard.features.appstate

import androidx.lifecycle.LiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppStateViewModel @Inject constructor(
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    fun getContent(): LiveData<Content> = appStateRepository.getContent()

    fun runAction(action: Action) {
        launch {
            appStateRepository.runAction(action)
        }
    }
}
