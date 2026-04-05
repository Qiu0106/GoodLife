package com.example.smartelderlycare_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartelderlycare_app.data.model.Post
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

/**
 * 帖子ViewModel
 * 处理帖子相关的UI状态和数据操作
 */
class PostViewModel : ViewModel() {

    private val repository = BmobRepository()

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // 成功提示
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // 帖子列表
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    // 当前帖子
    private val _currentPost = MutableLiveData<Post?>()
    val currentPost: LiveData<Post?> = _currentPost

    /**
     * 创建帖子
     */
    fun createPost(post: Post) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.createPost(post)
                .onSuccess { savedPost ->
                    _successMessage.value = "发布成功"
                    _currentPost.value = savedPost
                    // 刷新列表
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.value = "发布失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取用户的所有帖子
     */
    fun getPostsByUserId(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getPostsByUserId(userId)
                .onSuccess { posts ->
                    _posts.value = posts
                }
                .onFailure { error ->
                    _errorMessage.value = "获取数据失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取所有帖子
     */
    fun getAllPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getAllPosts()
                .onSuccess { posts ->
                    _posts.value = posts
                }
                .onFailure { error ->
                    _errorMessage.value = "获取数据失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 更新帖子
     */
    fun updatePost(objectId: String, post: Post) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.updatePost(objectId, post)
                .onSuccess { updatedPost ->
                    _successMessage.value = "更新成功"
                    _currentPost.value = updatedPost
                    // 刷新列表
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.value = "更新失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 删除帖子
     */
    fun deletePost(objectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.deletePost(objectId)
                .onSuccess {
                    _successMessage.value = "删除成功"
                    // 刷新列表
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.value = "删除失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 点赞帖子
     */
    fun likePost(objectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.incrementPostLikes(objectId)
                .onSuccess {
                    _successMessage.value = "点赞成功"
                    // 刷新列表
                    getAllPosts()
                }
                .onFailure { error ->
                    _errorMessage.value = "点赞失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 清除成功信息
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}
