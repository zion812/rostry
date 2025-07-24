package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.PostDao
import com.rio.rostry.data.model.Post
import com.rio.rostry.data.model.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    suspend fun getAllPosts(): List<Post> {
        return postDao.getAllPostsSync()
    }
    
    fun getAllPostsFlow(): Flow<List<Post>> {
        return postDao.getAllPosts()
    }
    
    suspend fun getPostById(postId: String): Post? {
        return postDao.getPostById(postId)
    }
    
    suspend fun getUserPosts(userId: String): List<Post> {
        return postDao.getUserPosts(userId)
    }
    
    suspend fun createPost(post: Post) {
        postDao.insertPost(post)
    }
    
    suspend fun updatePost(post: Post) {
        postDao.updatePost(post)
    }
    
    suspend fun deletePost(postId: String) {
        postDao.deletePost(postId)
    }
    
    suspend fun likePost(postId: String, userId: String) {
        val post = postDao.getPostById(postId)
        post?.let { existingPost ->
            val updatedLikes = if (existingPost.likes.contains(userId)) {
                existingPost.likes - userId
            } else {
                existingPost.likes + userId
            }
            
            val updatedPost = existingPost.copy(
                likes = updatedLikes,
                updatedAt = System.currentTimeMillis()
            )
            
            postDao.updatePost(updatedPost)
        }
    }
    
    suspend fun searchPosts(query: String): List<Post> {
        return postDao.searchPosts("%$query%")
    }
    
    suspend fun getPostsByType(type: String): List<Post> {
        return postDao.getPostsByType(type)
    }
    
    // Enhanced Social Features with Firebase
    fun getCommunityFeed(): Flow<List<Post>> = flow {
        try {
            val snapshot = firestore.collection("posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
            posts.forEach { postDao.insertPost(it) }
            emit(posts)
        } catch (e: Exception) {
            // Fallback to local data
            postDao.getAllPosts().collect { emit(it) }
        }
    }
    
    suspend fun createPostWithImage(post: Post, imageUri: android.net.Uri?): Result<String> {
        return try {
            val postId = post.id.ifEmpty { UUID.randomUUID().toString() }
            var imageUrl: String? = null
            
            // Upload image if provided
            imageUri?.let { uri ->
                val imageRef = storage.reference.child("post_images/$postId/${System.currentTimeMillis()}.jpg")
                val uploadTask = imageRef.putFile(uri).await()
                imageUrl = uploadTask.storage.downloadUrl.await().toString()
            }
            
            val postWithImage = post.copy(
                id = postId,
                imageUrls = if (imageUrl != null) listOf(imageUrl) else post.imageUrls
            )
            
            // Save to Firestore
            firestore.collection("posts").document(postId).set(postWithImage).await()
            
            // Save to local database
            postDao.insertPost(postWithImage)
            
            Result.success(postId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun likePostFirebase(postId: String, userId: String): Result<Unit> {
        return try {
            val postRef = firestore.collection("posts").document(postId)
            val snapshot = postRef.get().await()
            val post = snapshot.toObject(Post::class.java)
            
            post?.let { existingPost ->
                val updatedLikes = if (existingPost.likes.contains(userId)) {
                    existingPost.likes - userId
                } else {
                    existingPost.likes + userId
                }
                
                val updatedPost = existingPost.copy(
                    likes = updatedLikes,
                    updatedAt = System.currentTimeMillis()
                )
                
                postRef.set(updatedPost).await()
                postDao.updatePost(updatedPost)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addComment(comment: Comment): Result<String> {
        return try {
            val commentId = comment.id.ifEmpty { UUID.randomUUID().toString() }
            val commentWithId = comment.copy(id = commentId)
            
            firestore.collection("comments").document(commentId).set(commentWithId).await()
            
            // Update post comment count
            val postRef = firestore.collection("posts").document(comment.postId)
            val snapshot = postRef.get().await()
            val post = snapshot.toObject(Post::class.java)
            
            post?.let { existingPost ->
                val updatedPost = existingPost.copy(
                    comments = existingPost.comments + commentWithId,
                    updatedAt = System.currentTimeMillis()
                )
                postRef.set(updatedPost).await()
                postDao.updatePost(updatedPost)
            }
            
            Result.success(commentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getPostComments(postId: String): Flow<List<Comment>> = flow {
        try {
            val snapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()
            
            val comments = snapshot.documents.mapNotNull { it.toObject(Comment::class.java) }
            emit(comments)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun deletePostFirebase(postId: String): Result<Unit> {
        return try {
            // Delete post
            firestore.collection("posts").document(postId).delete().await()
            
            // Delete associated comments
            val commentsSnapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()
            
            commentsSnapshot.documents.forEach { doc ->
                doc.reference.delete()
            }
            
            // Delete from local database
            postDao.deletePost(postId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun searchPostsFirebase(query: String): Flow<List<Post>> = flow {
        try {
            val snapshot = firestore.collection("posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                .filter { post ->
                    post.content.contains(query, ignoreCase = true) ||
                    post.authorName.contains(query, ignoreCase = true) ||
                    post.location.contains(query, ignoreCase = true)
                }
            
            emit(posts)
        } catch (e: Exception) {
            // Fallback to local search
            val localPosts = postDao.searchPosts("%$query%")
            emit(localPosts)
        }
    }
}