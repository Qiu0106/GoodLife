package com.example.smartelderlycare_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {

    private val repository = BmobRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _currentPost = MutableLiveData<Post?>()
    val currentPost: LiveData<Post?> = _currentPost

    fun createPost(post: Post) {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.createPost(post)
                .onSuccess { savedPost ->
                    _successMessage.setValue("发布成功")
                    _currentPost.setValue(savedPost)
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.setValue("发布失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun getPostsByUserId(userId: String) {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.getPostsByUserId(userId)
                .onSuccess { posts ->
                    _posts.setValue(posts)
                }
                .onFailure { error ->
                    _errorMessage.setValue("获取数据失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun getAllPosts() {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.getAllPosts()
                .onSuccess { posts ->
                    _posts.setValue(posts)
                }
                .onFailure { error ->
                    _errorMessage.setValue("获取数据失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun getPostsByCategory(category: String) {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.getPostsByCategory(category)
                .onSuccess { posts ->
                    _posts.setValue(posts)
                }
                .onFailure { error ->
                    _errorMessage.setValue("获取数据失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun updatePost(objectId: String, post: Post) {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.updatePost(objectId, post)
                .onSuccess { updatedPost ->
                    _successMessage.setValue("更新成功")
                    _currentPost.setValue(updatedPost)
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.setValue("更新失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun deletePost(objectId: String) {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.deletePost(objectId)
                .onSuccess {
                    _successMessage.setValue("删除成功")
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.setValue("删除失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun likePost(objectId: String) {
        viewModelScope.launch {
            _isLoading.setValue(true)
            _errorMessage.setValue(null)

            repository.incrementPostLikes(objectId)
                .onSuccess {
                    _successMessage.setValue("点赞成功")
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.setValue("点赞失败: ${error.message}")
                }

            _isLoading.setValue(false)
        }
    }

    fun clearError() {
        _errorMessage.setValue(null)
    }

    fun clearSuccess() {
        _successMessage.setValue(null)
    }
}