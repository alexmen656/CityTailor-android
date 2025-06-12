package com.app.citytailor.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.citytailor.model.CommunityPost
import com.app.citytailor.network.PostService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(application: Application) : AndroidViewModel(application) {
    
    private val postService = PostService.shared
    
    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _showCreatePost = MutableStateFlow(false)
    val showCreatePost: StateFlow<Boolean> = _showCreatePost.asStateFlow()
    
    init {
        fetchPosts()
    }
    
    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fetchedPosts = postService.fetchPosts()
                _posts.value = fetchedPosts
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val fetchedPosts = postService.fetchPosts()
                _posts.value = fetchedPosts
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                val result = postService.likePost(postId)
                Log.d("CommunityViewModel", "Like result: success=${result.success}, likes=${result.likes}, hasLiked=${result.hasLiked}")
                if (result.success) {
                    val updatedPosts = _posts.value.map { post ->
                        if (post.id == postId) {
                            val updatedPost = post.copy(
                                hasLiked = result.hasLiked,
                                likes = result.likes
                            )
                            Log.d("CommunityViewModel", "Updated post: id=${updatedPost.id}, hasLiked=${updatedPost.hasLiked}, likes=${updatedPost.likes}")
                            updatedPost
                        } else {
                            post
                        }
                    }
                    _posts.value = updatedPosts
                    Log.d("CommunityViewModel", "Posts state updated")
                }
            } catch (e: Exception) {
                Log.e("CommunityViewModel", "Error liking post", e)
            }
        }
    }
    
    fun createPost(location: String, caption: String, images: List<Bitmap>) {
        viewModelScope.launch {
            try {
                val success = postService.createPost(location, caption, images)
                if (success) {
                    // Refresh posts to show the new post
                    fetchPosts()
                    _showCreatePost.value = false
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun setShowCreatePost(show: Boolean) {
        _showCreatePost.value = show
    }
    
    companion object {
        class Factory(private val application: Application) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
                    return CommunityViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
