package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.core.functional.SingleRunner
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateDataSource @Inject constructor(
    resourceProvider: ResourceProvider,
    private val navigationActionHandler: NavigationActionHandler,
    private val postActionHandler: PostActionHandler,
    private val searchActionHandler: SearchActionHandler,
    private val singleRunner: SingleRunner,
    private val connectivityManager: ConnectivityManager?
) : AppStateRepository {

    private val currentContent = MutableLiveData<Content>().apply {
        value = PostList(
            category = All,
            title = resourceProvider.getString(R.string.posts_title_all),
            posts = emptyList(),
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = true,
            isConnected = connectivityManager.isConnected()
        )
    }

    override fun getContent(): LiveData<Content> = currentContent

    override suspend fun runAction(action: Action) {
        singleRunner.afterPrevious {
            currentContent.value?.let { content ->
                currentContent.postValue(
                    when (action) {
                        is NavigationAction -> navigationActionHandler.runAction(action, content)
                        is PostAction -> postActionHandler.runAction(action, content)
                        is SearchAction -> searchActionHandler.runAction(action, content)
                    }
                )
            }
        }
    }
}
