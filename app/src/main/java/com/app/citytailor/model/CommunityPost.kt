package com.app.citytailor.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class CommunityPost(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("userAvatar")
    val userAvatar: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("caption")
    val caption: String,
    
    @SerializedName("imageUrls")
    val imageNames: List<String>? = emptyList(),
    
    @SerializedName("likes")
    val likes: Int = 0,
    
    @SerializedName("comments")
    val comments: Int = 0,
    
    @SerializedName("timestamp")
    val timestamp: Date = Date(),
    
    @SerializedName("hasLiked")
    val hasLiked: Boolean = false
)

data class LikeResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("likes")
    val likes: Int
)
