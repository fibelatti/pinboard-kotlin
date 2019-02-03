package com.fibelatti.pinboard.core.di.modules

import androidx.lifecycle.ViewModel
import com.fibelatti.pinboard.core.di.mapkeys.ViewModelKey
import com.fibelatti.pinboard.features.tags.data.TagsApi
import com.fibelatti.pinboard.features.tags.data.TagsDataSource
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.presentation.TagsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class TagsModule {

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun tagsApi(retrofit: Retrofit): TagsApi = retrofit.create()
    }

    @Binds
    abstract fun tagsRepository(tagsDataSource: TagsDataSource): TagsRepository

    @Binds
    @IntoMap
    @ViewModelKey(TagsViewModel::class)
    abstract fun tagsViewModel(tagsViewModel: TagsViewModel): ViewModel
}
