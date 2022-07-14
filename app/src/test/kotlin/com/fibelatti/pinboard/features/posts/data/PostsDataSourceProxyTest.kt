package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.pinboard.randomBoolean
import com.fibelatti.pinboard.randomInt
import com.fibelatti.pinboard.randomString
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PostsDataSourceProxyTest {

    private val postsDataSourcePinboardApi = mockk<PostsDataSourcePinboardApi>()
    private val postsDataSourceNoApi = mockk<PostsDataSourceNoApi>()
    private val userRepository = mockk<UserRepository>()

    private val booleanArg = randomBoolean()
    private val intArg = randomInt()
    private val stringArg = randomString()

    @Nested
    inner class MainVariantTest {

        private val proxy = PostsDataSourceProxy(
            postsDataSourcePinboardApi,
            postsDataSourceNoApi,
            userRepository,
            mainVariant = true,
        )

        @BeforeEach
        fun setup() {
            every { userRepository.appReviewMode } returns false
        }

        @Test
        fun `update calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<String>>()

            coEvery { postsDataSourcePinboardApi.update() } returns expectedResult

            val result = proxy.update()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `add calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Post>>()

            coEvery {
                postsDataSourcePinboardApi.add(
                    url = stringArg,
                    title = stringArg,
                    description = stringArg,
                    private = booleanArg,
                    readLater = booleanArg,
                    tags = any(),
                    replace = booleanArg,
                )
            } returns expectedResult

            val result = proxy.add(
                url = stringArg,
                title = stringArg,
                description = stringArg,
                private = booleanArg,
                readLater = booleanArg,
                tags = mockk(),
                replace = booleanArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `delete calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Unit>>()

            coEvery {
                postsDataSourcePinboardApi.delete(
                    url = stringArg,
                )
            } returns expectedResult

            val result = proxy.delete(
                url = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `getAllPosts calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<PostListResult>>()

            coEvery {
                postsDataSourcePinboardApi.getAllPosts(
                    newestFirst = booleanArg,
                    searchTerm = stringArg,
                    tags = any(),
                    untaggedOnly = booleanArg,
                    postVisibility = any(),
                    readLaterOnly = booleanArg,
                    countLimit = intArg,
                    pageLimit = intArg,
                    pageOffset = intArg,
                    forceRefresh = booleanArg,
                )
            } returns flowOf(expectedResult)

            val result = proxy.getAllPosts(
                newestFirst = booleanArg,
                searchTerm = stringArg,
                tags = mockk(),
                untaggedOnly = booleanArg,
                postVisibility = mockk(),
                readLaterOnly = booleanArg,
                countLimit = intArg,
                pageLimit = intArg,
                pageOffset = intArg,
                forceRefresh = booleanArg,
            )

            assertThat(result.first()).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `getQueryResultSize calls the expected data source`() = runTest {
            val expectedResult = 13

            coEvery {
                postsDataSourcePinboardApi.getQueryResultSize(
                    searchTerm = stringArg,
                    tags = any(),
                )
            } returns expectedResult

            val result = proxy.getQueryResultSize(
                searchTerm = stringArg,
                tags = mockk(),
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `getPost calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Post>>()

            coEvery {
                postsDataSourcePinboardApi.getPost(
                    url = stringArg,
                )
            } returns expectedResult

            val result = proxy.getPost(
                url = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `searchExistingPostTag calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<List<String>>>()

            coEvery {
                postsDataSourcePinboardApi.searchExistingPostTag(
                    tag = stringArg,
                )
            } returns expectedResult

            val result = proxy.searchExistingPostTag(
                tag = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `getPendingSyncPosts calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<List<Post>>>()

            coEvery {
                postsDataSourcePinboardApi.getPendingSyncPosts()
            } returns expectedResult

            val result = proxy.getPendingSyncPosts()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }

        @Test
        fun `clearCache calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Unit>>()

            coEvery {
                postsDataSourcePinboardApi.clearCache()
            } returns expectedResult

            val result = proxy.clearCache()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourceNoApi wasNot Called }
        }
    }

    @Nested
    inner class AppReviewModeTest {

        private val proxy = PostsDataSourceProxy(
            postsDataSourcePinboardApi,
            postsDataSourceNoApi,
            userRepository,
            mainVariant = true,
        )

        @BeforeEach
        fun setup() {
            every { userRepository.appReviewMode } returns true
        }

        @Test
        fun `update calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<String>>()

            coEvery { postsDataSourceNoApi.update() } returns expectedResult

            val result = proxy.update()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `add calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Post>>()

            coEvery {
                postsDataSourceNoApi.add(
                    url = stringArg,
                    title = stringArg,
                    description = stringArg,
                    private = booleanArg,
                    readLater = booleanArg,
                    tags = any(),
                    replace = booleanArg,
                )
            } returns expectedResult

            val result = proxy.add(
                url = stringArg,
                title = stringArg,
                description = stringArg,
                private = booleanArg,
                readLater = booleanArg,
                tags = mockk(),
                replace = booleanArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `delete calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Unit>>()

            coEvery {
                postsDataSourceNoApi.delete(
                    url = stringArg,
                )
            } returns expectedResult

            val result = proxy.delete(
                url = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getAllPosts calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<PostListResult>>()

            coEvery {
                postsDataSourceNoApi.getAllPosts(
                    newestFirst = booleanArg,
                    searchTerm = stringArg,
                    tags = any(),
                    untaggedOnly = booleanArg,
                    postVisibility = any(),
                    readLaterOnly = booleanArg,
                    countLimit = intArg,
                    pageLimit = intArg,
                    pageOffset = intArg,
                    forceRefresh = booleanArg,
                )
            } returns flowOf(expectedResult)

            val result = proxy.getAllPosts(
                newestFirst = booleanArg,
                searchTerm = stringArg,
                tags = mockk(),
                untaggedOnly = booleanArg,
                postVisibility = mockk(),
                readLaterOnly = booleanArg,
                countLimit = intArg,
                pageLimit = intArg,
                pageOffset = intArg,
                forceRefresh = booleanArg,
            )

            assertThat(result.first()).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getQueryResultSize calls the expected data source`() = runTest {
            val expectedResult = 13

            coEvery {
                postsDataSourceNoApi.getQueryResultSize(
                    searchTerm = stringArg,
                    tags = any(),
                )
            } returns expectedResult

            val result = proxy.getQueryResultSize(
                searchTerm = stringArg,
                tags = mockk(),
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getPost calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Post>>()

            coEvery {
                postsDataSourceNoApi.getPost(
                    url = stringArg,
                )
            } returns expectedResult

            val result = proxy.getPost(
                url = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `searchExistingPostTag calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<List<String>>>()

            coEvery {
                postsDataSourceNoApi.searchExistingPostTag(
                    tag = stringArg,
                )
            } returns expectedResult

            val result = proxy.searchExistingPostTag(
                tag = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getPendingSyncPosts calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<List<Post>>>()

            coEvery {
                postsDataSourceNoApi.getPendingSyncPosts()
            } returns expectedResult

            val result = proxy.getPendingSyncPosts()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `clearCache calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Unit>>()

            coEvery {
                postsDataSourceNoApi.clearCache()
            } returns expectedResult

            val result = proxy.clearCache()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }
    }

    @Nested
    inner class NotMainVariantTest {

        private val proxy = PostsDataSourceProxy(
            postsDataSourcePinboardApi,
            postsDataSourceNoApi,
            userRepository,
            mainVariant = false,
        )

        @Test
        fun `update calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<String>>()

            coEvery { postsDataSourceNoApi.update() } returns expectedResult

            val result = proxy.update()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `add calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Post>>()

            coEvery {
                postsDataSourceNoApi.add(
                    url = stringArg,
                    title = stringArg,
                    description = stringArg,
                    private = booleanArg,
                    readLater = booleanArg,
                    tags = any(),
                    replace = booleanArg,
                )
            } returns expectedResult

            val result = proxy.add(
                url = stringArg,
                title = stringArg,
                description = stringArg,
                private = booleanArg,
                readLater = booleanArg,
                tags = mockk(),
                replace = booleanArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `delete calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Unit>>()

            coEvery {
                postsDataSourceNoApi.delete(
                    url = stringArg,
                )
            } returns expectedResult

            val result = proxy.delete(
                url = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getAllPosts calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<PostListResult>>()

            coEvery {
                postsDataSourceNoApi.getAllPosts(
                    newestFirst = booleanArg,
                    searchTerm = stringArg,
                    tags = any(),
                    untaggedOnly = booleanArg,
                    postVisibility = any(),
                    readLaterOnly = booleanArg,
                    countLimit = intArg,
                    pageLimit = intArg,
                    pageOffset = intArg,
                    forceRefresh = booleanArg,
                )
            } returns flowOf(expectedResult)

            val result = proxy.getAllPosts(
                newestFirst = booleanArg,
                searchTerm = stringArg,
                tags = mockk(),
                untaggedOnly = booleanArg,
                postVisibility = mockk(),
                readLaterOnly = booleanArg,
                countLimit = intArg,
                pageLimit = intArg,
                pageOffset = intArg,
                forceRefresh = booleanArg,
            )

            assertThat(result.first()).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getQueryResultSize calls the expected data source`() = runTest {
            val expectedResult = 13

            coEvery {
                postsDataSourceNoApi.getQueryResultSize(
                    searchTerm = stringArg,
                    tags = any(),
                )
            } returns expectedResult

            val result = proxy.getQueryResultSize(
                searchTerm = stringArg,
                tags = mockk(),
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getPost calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Post>>()

            coEvery {
                postsDataSourceNoApi.getPost(
                    url = stringArg,
                )
            } returns expectedResult

            val result = proxy.getPost(
                url = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `searchExistingPostTag calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<List<String>>>()

            coEvery {
                postsDataSourceNoApi.searchExistingPostTag(
                    tag = stringArg,
                )
            } returns expectedResult

            val result = proxy.searchExistingPostTag(
                tag = stringArg,
            )

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `getPendingSyncPosts calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<List<Post>>>()

            coEvery {
                postsDataSourceNoApi.getPendingSyncPosts()
            } returns expectedResult

            val result = proxy.getPendingSyncPosts()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }

        @Test
        fun `clearCache calls the expected data source`() = runTest {
            val expectedResult = mockk<Result<Unit>>()

            coEvery {
                postsDataSourceNoApi.clearCache()
            } returns expectedResult

            val result = proxy.clearCache()

            assertThat(result).isEqualTo(expectedResult)
            coVerify { postsDataSourcePinboardApi wasNot Called }
        }
    }
}
