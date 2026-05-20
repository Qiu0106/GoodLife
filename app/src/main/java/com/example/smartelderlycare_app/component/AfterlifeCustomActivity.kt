package com.example.smartelderlycare_app.component

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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

    private lateinit var layoutMenu: LinearLayout
    private lateinit var layoutContent: LinearLayout
    private lateinit var cardFuneralPlan: CardView
    private lateinit var cardCemeterySelect: CardView
    private lateinit var btnBack: TextView
    private lateinit var tvContentTitle: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var btnSave: Button
    private lateinit var adapter: AfterlifePagerAdapter
    private lateinit var fragmentCemeteryContainer: android.widget.FrameLayout
    private lateinit var layoutBottomButtons: LinearLayout

    private val viewModel: AfterlifePlanViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null
    private var visitGraveFragment: VisitGraveFragment? = null

    companion object {
        private const val PAGE_FUNERAL_PLAN = 0
        private const val PAGE_CEMETERY_SELECT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_afterlife_custom)

        layoutMenu = findViewById(R.id.layoutMenu)
        layoutContent = findViewById(R.id.layoutContent)
        cardFuneralPlan = findViewById(R.id.cardFuneralPlan)
        cardCemeterySelect = findViewById(R.id.cardCemeterySelect)
        btnBack = findViewById(R.id.btnBack)
        tvContentTitle = findViewById(R.id.tvContentTitle)
        viewPager = findViewById(R.id.viewPager)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnSave = findViewById(R.id.btnSave)
        fragmentCemeteryContainer = findViewById(R.id.fragmentCemeteryContainer)
        layoutBottomButtons = findViewById(R.id.layoutBottomButtons)

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

        cardFuneralPlan.setOnClickListener { navigateTo(PAGE_FUNERAL_PLAN) }
        cardCemeterySelect.setOnClickListener { navigateTo(PAGE_CEMETERY_SELECT) }
        btnBack.setOnClickListener { goBackToMenu() }

        observeViewModel()
    }

    private fun navigateTo(page: Int) {
        layoutMenu.visibility = View.GONE
        layoutContent.visibility = View.VISIBLE

        if (page == PAGE_FUNERAL_PLAN) {
            tvContentTitle.text = "治丧规划"
            viewPager.visibility = View.VISIBLE
            fragmentCemeteryContainer.visibility = View.GONE
            layoutBottomButtons.visibility = View.VISIBLE
            updateButtons(viewPager.currentItem)
        } else {
            tvContentTitle.text = "陵园选址"
            viewPager.visibility = View.GONE
            fragmentCemeteryContainer.visibility = View.VISIBLE
            layoutBottomButtons.visibility = View.GONE

            if (visitGraveFragment == null) {
                visitGraveFragment = VisitGraveFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentCemeteryContainer, visitGraveFragment!!)
                    .commit()
            }
        }
    }

    private fun goBackToMenu() {
        layoutContent.visibility = View.GONE
        layoutMenu.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (layoutContent.visibility == View.VISIBLE) {
            goBackToMenu()
        } else {
            super.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })

        viewModel.successMessage.observe(this, Observer { successMessage ->
            successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
                finish()
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
        btnPrevious.visibility = if (position > 0) View.VISIBLE else View.GONE

        if (position == adapter.itemCount - 1) {
            btnNext.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
        } else {
            btnNext.visibility = View.VISIBLE
            btnSave.visibility = View.GONE
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
        val visitInfo = visitGraveFragment?.getVisitInfo() ?: mapOf("date" to null, "time" to null, "notes" to null, "visitName" to null)

        val userId = getSharedPreferences("user", MODE_PRIVATE).getString("userId", "anonymous") ?: "anonymous"

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

        saveToBmob(afterlifePlan, visitInfo, userId)
    }

    private fun saveToBmob(plan: AfterlifePlan, visitInfo: Map<String, String?>, userId: String) {
        viewModel.currentPlan.observe(this, Observer { savedPlan ->
            savedPlan?.let {
                val visitInfoObj = AfterlifePlanVisitInfo(
                    planId = savedPlan.id?.toString() ?: "",
                    userId = userId,
                    visitDate = visitInfo["date"],
                    visitTime = visitInfo["time"],
                    visitNotes = visitInfo["notes"],
                    visitName = visitInfo["visitName"]
                )
                viewModel.createVisitInfo(visitInfoObj)
                viewModel.currentPlan.removeObservers(this)
            }
        })

        viewModel.createAfterlifePlan(plan)
    }
}