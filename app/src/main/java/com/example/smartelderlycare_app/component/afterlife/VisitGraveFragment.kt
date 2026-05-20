package com.example.smartelderlycare_app.component.afterlife

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.component.cemetery.CemeteryMapActivity
import com.example.smartelderlycare_app.data.model.AfterlifePlanVisitInfo
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.launch

class VisitGraveFragment : Fragment() {

    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etVisitName: EditText
    private lateinit var btnSaveVisitInfo: Button
    private lateinit var btnViewCemeteryMap: Button

    private val repository = BmobRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visit_grave, container, false)
        etDate = view.findViewById(R.id.etDate)
        etTime = view.findViewById(R.id.etTime)
        etVisitName = view.findViewById(R.id.etVisitName)
        btnSaveVisitInfo = view.findViewById(R.id.btnSaveVisitInfo)
        btnViewCemeteryMap = view.findViewById(R.id.btnViewCemeteryMap)

        btnSaveVisitInfo.setOnClickListener {
            saveVisitInfo()
        }

        btnViewCemeteryMap.setOnClickListener {
            startActivity(Intent(requireContext(), CemeteryMapActivity::class.java))
        }

        loadSavedVisitInfo()

        return view
    }

    private fun loadSavedVisitInfo() {
        val prefs = requireContext().getSharedPreferences("visit_info", Context.MODE_PRIVATE)
        etDate.setText(prefs.getString("visitDate", ""))
        etTime.setText(prefs.getString("visitTime", ""))
        etVisitName.setText(prefs.getString("visitName", ""))
    }

    private fun saveVisitInfo() {
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()
        val visitName = etVisitName.text.toString().trim()

        if (date.isEmpty() && time.isEmpty() && visitName.isEmpty()) {
            Toast.makeText(requireContext(), "请至少填写一项信息", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = requireContext().getSharedPreferences("visit_info", Context.MODE_PRIVATE).edit()
        prefs.putString("visitDate", date)
        prefs.putString("visitTime", time)
        prefs.putString("visitName", visitName)
        prefs.apply()

        val user = repository.getCurrentUser()
        if (user == null) {
            Toast.makeText(requireContext(), "请先登录后再同步到云端", Toast.LENGTH_SHORT).show()
            return
        }

        val visitInfo = AfterlifePlanVisitInfo(
            planId = "",
            userId = user.objectId ?: user.phone,
            visitDate = date.takeIf { it.isNotEmpty() },
            visitTime = time.takeIf { it.isNotEmpty() },
            visitName = visitName.takeIf { it.isNotEmpty() }
        )

        lifecycleScope.launch {
            btnSaveVisitInfo.isEnabled = false
            btnSaveVisitInfo.text = "保存中..."

            val result = repository.createVisitInfo(visitInfo)
            result.onSuccess {
                Toast.makeText(requireContext(), "保存成功并已同步到云端", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "本地保存成功，云端同步失败: ${e.message}", Toast.LENGTH_LONG).show()
            }

            btnSaveVisitInfo.isEnabled = true
            btnSaveVisitInfo.text = "💾 保存预约信息"
        }
    }

    fun getVisitInfo(): Map<String, String?> {
        return mapOf(
            "date" to etDate.text.toString(),
            "time" to etTime.text.toString(),
            "visitName" to etVisitName.text.toString().takeIf { it.isNotEmpty() }
        )
    }
}
