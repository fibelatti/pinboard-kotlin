package com.fibelatti.pinboard.core.di.modules

import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.posts.data.PostsDataSource
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class PostsModule {

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun postsApi(retrofit: Retrofit): PostsApi = retrofit.create()
    }

    @Binds
    abstract fun postsRepository(postsDataSource: PostsDataSource): PostsRepository
}
