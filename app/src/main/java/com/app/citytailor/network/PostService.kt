package com.app.citytailor.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.app.citytailor.model.CommunityPost
import com.app.citytailor.model.LikeResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PostService private constructor() {
    
    companion object {
        val shared = PostService()
        private const val BASE_URL = "https://alex.polan.sk/ct/backend/posts"
        private const val IMAGE_BASE_URL = "https://alex.polan.sk/ct/backend"
    }
    
    private val client = OkHttpClient()
    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateTypeAdapter())
        .create()
    
    suspend fun fetchPosts(): List<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/posts.php"
            Log.d("PostService", "Fetching posts from: $url")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("X-User-Name", "user13") // Send username in header like iOS
                .build()
            
            val response = client.newCall(request).await()
            
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                Log.d("PostService", "Fetch posts successful: $responseBody")
                val listType: Type = object : TypeToken<List<CommunityPost>>() {}.type
                return@withContext gson.fromJson<List<CommunityPost>>(responseBody.ifEmpty { "[]" }, listType)
            } else {
                Log.e("PostService", "Failed to fetch posts: ${response.code}")
                Log.e("PostService", "Error response body: $responseBody")
                return@withContext emptyList<CommunityPost>()
            }
        } catch (e: Exception) {
            Log.e("PostService", "Error fetching posts", e)
            return@withContext emptyList<CommunityPost>()
        }
    }
    
    suspend fun likePost(postId: String): LikeResponse = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/likes.php"
            Log.d("PostService", "Liking post at: $url with postId: $postId")
            
            val likeRequest = mapOf("postId" to postId)
            val json = gson.toJson(likeRequest)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-User-Name", "user13") // Send username in header like iOS
                .build()
            
            val response = client.newCall(request).await()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                Log.d("PostService", "Like post successful: $responseBody")
                return@withContext gson.fromJson(responseBody, LikeResponse::class.java)
            } else {
                Log.e("PostService", "Failed to like post: ${response.code}")
                Log.e("PostService", "Error response body: $responseBody")
                return@withContext LikeResponse(success = false, likes = 0)
            }
        } catch (e: Exception) {
            Log.e("PostService", "Error liking post", e)
            return@withContext LikeResponse(success = false, likes = 0)
        }
    }
    
    suspend fun createPost(
        location: String,
        caption: String,
        images: List<Bitmap>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/posts.php"
            Log.d("PostService", "Creating post at: $url")
            
            val base64Images = images.map { bitmap ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
            
            // Remove username from the request body
            val createPostRequest = mapOf(
                "location" to location,
                "caption" to caption,
                "images" to base64Images
            )
            
            val json = gson.toJson(createPostRequest)
            Log.d("PostService", "Request payload: $json")
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-User-Name", "user13") // Send username in header like iOS
                .build()
            
            val response = client.newCall(request).await()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                Log.d("PostService", "Post created successfully: $responseBody")
                return@withContext true
            } else {
                Log.e("PostService", "Failed to create post: ${response.code}")
                Log.e("PostService", "Error response body: $responseBody")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("PostService", "Error creating post", e)
            return@withContext false
        }
    }
}

// Extension function to make OkHttp calls suspendable
private suspend fun Call.await(): Response {
    return suspendCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }
            
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}
