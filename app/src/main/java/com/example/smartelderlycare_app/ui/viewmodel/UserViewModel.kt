package com.example.smartelderlycare_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartelderlycare_app.data.model.User
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

/**
 * 用户ViewModel
 * 处理用户登录、注册和信息管理的UI状态
 */
class UserViewModel : ViewModel() {

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

    // 当前登录用户
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    /**
     * 用户登录
     */
    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.login(phone, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _successMessage.value = "登录成功"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "登录失败"
                }

            _isLoading.value = false
        }
    }

    /**
     * 用户注册
     */
    fun register(phone: String, password: String, nickname: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.register(phone, password, nickname)
                .onSuccess { user ->
                    _currentUser.value = user
                    _successMessage.value = "注册成功"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "注册失败"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取用户信息
     */
    fun getUserByPhone(phone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getUserByPhone(phone)
                .onSuccess { user ->
                    _currentUser.value = user
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "获取用户信息失败"
                }

            _isLoading.value = false
        }
    }

    /**
     * 更新用户信息
     */
    fun updateUser(objectId: String, user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.updateUser(objectId, user)
                .onSuccess { updatedUser ->
                    _successMessage.value = "更新成功"
                    _currentUser.value = updatedUser
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "更新失败"
                }

            _isLoading.value = false
        }
    }

    /**
     * 修改密码
     */
    fun changePassword(phone: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.changePassword(phone, oldPassword, newPassword)
                .onSuccess {
                    _successMessage.value = "密码修改成功"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "密码修改失败"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取当前登录用户（从Bmob缓存）
     */
    fun fetchCurrentUser() {
        val user = repository.getCurrentUser()
        _currentUser.value = user
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }

    /**
     * 退出登录
     */
    fun logout() {
        repository.logout()
        _currentUser.value = null
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
