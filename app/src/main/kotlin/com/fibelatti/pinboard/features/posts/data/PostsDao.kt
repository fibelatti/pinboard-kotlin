package com.fibelatti.pinboard.features.posts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fibelatti.pinboard.features.posts.data.model.POST_TABLE_NAME
import com.fibelatti.pinboard.features.posts.data.model.PostDto

@Dao
interface PostsDao {

    @Query("select count(*) from $POST_TABLE_NAME")
    fun getPostCount(): Int

    @Query("select * from $POST_TABLE_NAME")
    fun getAllPosts(): List<PostDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePosts(posts: List<PostDto>)

    @Query("delete from $POST_TABLE_NAME")
    fun deleteAllPosts()
}
