package com.example.smartelderlycare_app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartelderlycare_app.data.model.AfterlifePlan
import com.example.smartelderlycare_app.data.model.AfterlifePlanVisitInfo
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

/**
 * 身后事计划ViewModel
 * 处理身后事计划相关的UI状态和数据操作
 */
class AfterlifePlanViewModel : ViewModel() {

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

    // 身后事计划列表
    private val _afterlifePlans = MutableLiveData<List<AfterlifePlan>>()
    val afterlifePlans: LiveData<List<AfterlifePlan>> = _afterlifePlans

    // 当前编辑的计划
    private val _currentPlan = MutableLiveData<AfterlifePlan?>()
    val currentPlan: LiveData<AfterlifePlan?> = _currentPlan

    // 祭拜信息列表
    private val _visitInfos = MutableLiveData<List<AfterlifePlanVisitInfo>>()
    val visitInfos: LiveData<List<AfterlifePlanVisitInfo>> = _visitInfos

    // 当前祭拜信息
    private val _currentVisitInfo = MutableLiveData<AfterlifePlanVisitInfo?>()
    val currentVisitInfo: LiveData<AfterlifePlanVisitInfo?> = _currentVisitInfo

    /**
     * 创建身后事计划
     */
    fun createAfterlifePlan(plan: AfterlifePlan) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.createAfterlifePlan(plan)
                .onSuccess { savedPlan ->
                    _successMessage.value = "保存成功"
                    _currentPlan.value = savedPlan
                    // 刷新列表
                    getAllAfterlifePlans()
                }
                .onFailure { error ->
                    _errorMessage.value = "保存失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取用户的所有身后事计划
     */
    fun getAfterlifePlansByUserId(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getAfterlifePlansByUserId(userId)
                .onSuccess { plans ->
                    _afterlifePlans.value = plans
                }
                .onFailure { error ->
                    _errorMessage.value = "获取数据失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取所有身后事计划
     */
    fun getAllAfterlifePlans() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getAllAfterlifePlans()
                .onSuccess { plans ->
                    _afterlifePlans.value = plans
                }
                .onFailure { error ->
                    _errorMessage.value = "获取数据失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 更新身后事计划
     */
    fun updateAfterlifePlan(objectId: String, plan: AfterlifePlan) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.updateAfterlifePlan(objectId, plan)
                .onSuccess { updatedPlan ->
                    _successMessage.value = "更新成功"
                    _currentPlan.value = updatedPlan
                    // 刷新列表
                    getAllAfterlifePlans()
                }
                .onFailure { error ->
                    _errorMessage.value = "更新失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 删除身后事计划
     */
    fun deleteAfterlifePlan(objectId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.deleteAfterlifePlan(objectId)
                .onSuccess {
                    _successMessage.value = "删除成功"
                    // 刷新列表
                    getAllAfterlifePlans()
                }
                .onFailure { error ->
                    _errorMessage.value = "删除失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    // ==================== 祭拜信息操作 ====================

    /**
     * 创建祭拜信息
     */
    fun createVisitInfo(info: AfterlifePlanVisitInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.createVisitInfo(info)
                .onSuccess { savedInfo ->
                    _successMessage.value = "祭拜信息保存成功"
                    _currentVisitInfo.value = savedInfo
                    // 刷新列表
                    getVisitInfoByUserId(info.userId)
                }
                .onFailure { error ->
                    _errorMessage.value = "祭拜信息保存失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取指定计划的祭拜信息
     */
    fun getVisitInfoByPlanId(planId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getVisitInfoByPlanId(planId)
                .onSuccess { info ->
                    _currentVisitInfo.value = info
                }
                .onFailure { error ->
                    _errorMessage.value = "获取祭拜信息失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 获取用户的所有祭拜信息
     */
    fun getVisitInfoByUserId(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.getVisitInfoByUserId(userId)
                .onSuccess { infos ->
                    _visitInfos.value = infos
                }
                .onFailure { error ->
                    _errorMessage.value = "获取祭拜信息失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 更新祭拜信息
     */
    fun updateVisitInfo(objectId: String, info: AfterlifePlanVisitInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.updateVisitInfo(objectId, info)
                .onSuccess { updatedInfo ->
                    _successMessage.value = "祭拜信息更新成功"
                    _currentVisitInfo.value = updatedInfo
                    // 刷新列表
                    getVisitInfoByUserId(info.userId)
                }
                .onFailure { error ->
                    _errorMessage.value = "祭拜信息更新失败: ${error.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * 删除祭拜信息
     */
    fun deleteVisitInfo(objectId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.deleteVisitInfo(objectId)
                .onSuccess {
                    _successMessage.value = "祭拜信息删除成功"
                    // 刷新列表
                    getVisitInfoByUserId(userId)
                }
                .onFailure { error ->
                    _errorMessage.value = "祭拜信息删除失败: ${error.message}"
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
