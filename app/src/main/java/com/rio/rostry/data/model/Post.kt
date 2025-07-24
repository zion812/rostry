package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfileImage: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val likes: List<String> = emptyList(), // User IDs who liked
    val comments: List<Comment> = emptyList(),
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Comment class is defined in Comment.kt to avoid redeclaration