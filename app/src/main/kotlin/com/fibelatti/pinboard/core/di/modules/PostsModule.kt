package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.core.di.mapkeys.ViewModelKey
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.posts.data.PostsDataSource
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
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

    @Binds
    @IntoMap
    @ViewModelKey(PostListViewModel::class)
    abstract fun postListViewModel(postListViewModel: PostListViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentKey(PostListFragment::class)
    abstract fun postListFragment(postListFragment: PostListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostDetailFragment::class)
    abstract fun postDetailFragment(postDetailFragment: PostDetailFragment): Fragment
}
