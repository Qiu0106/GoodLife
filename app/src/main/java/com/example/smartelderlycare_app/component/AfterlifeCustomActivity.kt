package com.example.smartelderlycare_app.component

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.component.afterlife.AfterlifePagerAdapter
import com.example.smartelderlycare_app.component.afterlife.BasicInfoFragment
import com.example.smartelderlycare_app.component.afterlife.FuneralStyleFragment
import com.example.smartelderlycare_app.component.afterlife.RelicsBurialFragment
import com.example.smartelderlycare_app.component.afterlife.FuneralSuppliesFragment
import com.example.smartelderlycare_app.component.afterlife.BGMFragment
import com.example.smartelderlycare_app.component.afterlife.VisitGraveFragment
import com.example.smartelderlycare_app.data.model.AfterlifePlan
import com.example.smartelderlycare_app.data.model.AfterlifePlanVisitInfo
import com.example.smartelderlycare_app.ui.viewmodel.AfterlifePlanViewModel

class AfterlifeCustomActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnSave: Button
    private lateinit var adapter: AfterlifePagerAdapter
    
    private val viewModel: AfterlifePlanViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afterlife_custom)

        viewPager = findViewById(R.id.viewPager)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnSave = findViewById(R.id.btnSave)

        adapter = AfterlifePagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtons(position)
            }
        })

        btnPrevious.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            }
        }

        btnSave.setOnClickListener {
            showConfirmationDialog()
        }
        
        // 观察ViewModel状态
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // 观察加载状态
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        })
        
        // 观察错误信息
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })
        
        // 观察成功信息
        viewModel.successMessage.observe(this, Observer { successMessage ->
            successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
                finish() // 保存成功后关闭页面
            }
        })
    }
    
    private fun showProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(this).apply {
            setMessage("正在保存...")
            setCancelable(false)
            show()
        }
    }
    
    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun updateButtons(position: Int) {
        btnPrevious.visibility = if (position > 0) android.view.View.VISIBLE else android.view.View.GONE
        
        if (position == adapter.itemCount - 1) {
            btnNext.visibility = android.view.View.GONE
            btnSave.visibility = android.view.View.VISIBLE
        } else {
            btnNext.visibility = android.view.View.VISIBLE
            btnSave.visibility = android.view.View.GONE
        }
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认保存")
            .setMessage("您确定要保存当前配置吗？此操作不可逆。")
            .setPositiveButton("确定") { _, _ ->
                saveConfiguration()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveConfiguration() {
        val basicInfo = (adapter.getFragment(0) as BasicInfoFragment).getBasicInfo()
        val funeralStyle = (adapter.getFragment(1) as FuneralStyleFragment).getFuneralStyle()
        val relicsHandling = (adapter.getFragment(2) as RelicsBurialFragment).getRelicsHandling()
        val burialMethod = (adapter.getFragment(2) as RelicsBurialFragment).getBurialMethod()
        val supplies = (adapter.getFragment(3) as FuneralSuppliesFragment).getSelectedSupplies()
        val bgm = (adapter.getFragment(4) as BGMFragment).getSelectedBGM()
        val visitInfo = (adapter.getFragment(5) as VisitGraveFragment).getVisitInfo()

        // 获取当前用户ID（这里使用设备标识作为临时用户ID，实际项目中应该使用登录用户的ID）
        val userId = getSharedPreferences("user", MODE_PRIVATE).getString("userId", "anonymous") ?: "anonymous"

        // 创建AfterlifePlan对象
        val afterlifePlan = AfterlifePlan(
            userId = userId,
            name = basicInfo["name"] ?: "",
            age = basicInfo["age"] ?: "",
            contact = basicInfo["contact"] ?: "",
            biography = basicInfo["biography"],
            funeralStyle = funeralStyle,
            funeralLocation = null,
            funeralDate = null,
            funeralTime = null,
            relicsHandling = relicsHandling,
            burialMethod = burialMethod,
            burialLocation = null,
            coffin = supplies.contains("棺材"),
            urn = supplies.contains("骨灰盒"),
            flowers = supplies.contains("鲜花"),
            candles = supplies.contains("蜡烛"),
            photos = supplies.contains("照片"),
            otherSupplies = null,
            bgm = bgm,
            bgmNotes = null,
            visitDate = visitInfo["date"],
            visitTime = visitInfo["time"],
            visitNotes = null
        )

        // 保存到本地SharedPreferences（绑定当前userId）
        val sharedPreferences = getSharedPreferences("afterlife", MODE_PRIVATE).edit()
        sharedPreferences.putString("cachedUserId", userId)
        sharedPreferences.putString("name", basicInfo["name"])
        sharedPreferences.putString("age", basicInfo["age"])
        sharedPreferences.putString("contact", basicInfo["contact"])
        sharedPreferences.putString("funeralStyle", funeralStyle)
        sharedPreferences.putString("relicsHandling", relicsHandling)
        sharedPreferences.putString("burialMethod", burialMethod)
        sharedPreferences.putStringSet("supplies", supplies.toSet())
        sharedPreferences.putString("bgm", bgm)
        sharedPreferences.putString("visitDate", visitInfo["date"])
        sharedPreferences.putString("visitTime", visitInfo["time"])
        sharedPreferences.putBoolean("hasPlan", true)
        sharedPreferences.apply()

        // 上传到Bmob服务器 - 先保存基本信息，成功后保存祭拜信息
        saveToBmob(afterlifePlan, visitInfo, userId)
    }

    private fun saveToBmob(plan: AfterlifePlan, visitInfo: Map<String, String?>, userId: String) {
        // 观察计划保存结果
        viewModel.currentPlan.observe(this, Observer { savedPlan ->
            savedPlan?.let {
                // 基本信息保存成功，现在保存祭拜信息到 AfterlifePlanBmob_V 表
                val visitInfoObj = AfterlifePlanVisitInfo(
                    planId = savedPlan.id?.toString() ?: "",
                    userId = userId,
                    visitDate = visitInfo["date"],
                    visitTime = visitInfo["time"],
                    visitNotes = visitInfo["notes"]
                )
                viewModel.createVisitInfo(visitInfoObj)
                // 移除观察者，避免重复触发
                viewModel.currentPlan.removeObservers(this)
            }
        })

        // 开始保存基本信息
        viewModel.createAfterlifePlan(plan)
    }
}
