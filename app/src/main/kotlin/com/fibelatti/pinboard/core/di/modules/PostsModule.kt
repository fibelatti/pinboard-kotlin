package com.fibelatti.pinboard.core.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.fibelatti.pinboard.core.di.mapkeys.FragmentKey
import com.fibelatti.pinboard.core.di.mapkeys.ViewModelKey
import com.fibelatti.pinboard.features.posts.data.PostsApi
import com.fibelatti.pinboard.features.posts.data.PostsDataSource
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.presentation.PostAddFragment
import com.fibelatti.pinboard.features.posts.presentation.PostAddViewModel
import com.fibelatti.pinboard.features.posts.presentation.PostDetailFragment
import com.fibelatti.pinboard.features.posts.presentation.PostDetailViewModel
import com.fibelatti.pinboard.features.posts.presentation.PostListFragment
import com.fibelatti.pinboard.features.posts.presentation.PostListViewModel
import com.fibelatti.pinboard.features.posts.presentation.PostSearchFragment
import com.fibelatti.pinboard.features.share.ShareReceiverViewModel
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
    @ViewModelKey(PostDetailViewModel::class)
    abstract fun postDetailViewModel(postDetailsViewModel: PostDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentKey(PostDetailFragment::class)
    abstract fun postDetailFragment(postDetailFragment: PostDetailFragment): Fragment

    @Binds
    @IntoMap
    @ViewModelKey(PostAddViewModel::class)
    abstract fun postAddViewModel(postAddViewModel: PostAddViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentKey(PostAddFragment::class)
    abstract fun postAddFragment(postAddFragment: PostAddFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PostSearchFragment::class)
    abstract fun postSearchFragment(postSearchFragment: PostSearchFragment): Fragment

    @Binds
    @IntoMap
    @ViewModelKey(ShareReceiverViewModel::class)
    abstract fun shareReceiverViewModel(shareReceiverViewModel: ShareReceiverViewModel): ViewModel
}
