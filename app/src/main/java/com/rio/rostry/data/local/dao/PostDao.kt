package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?
    
    @Query("SELECT * FROM posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<Post>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)
    
    @Update
    suspend fun updatePost(post: Post)
    
    @Delete
    suspend fun deletePost(post: Post)
    
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
    
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
    
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    suspend fun getAllPostsSync(): List<Post>
    
    @Query("SELECT * FROM posts WHERE authorId = :userId ORDER BY createdAt DESC")
    suspend fun getUserPosts(userId: String): List<Post>
    
    @Query("SELECT * FROM posts WHERE content LIKE :query ORDER BY createdAt DESC")
    suspend fun searchPosts(query: String): List<Post>
    
    @Query("SELECT * FROM posts WHERE content LIKE '%' || :type || '%' ORDER BY createdAt DESC")
    suspend fun getPostsByType(type: String): List<Post>
}