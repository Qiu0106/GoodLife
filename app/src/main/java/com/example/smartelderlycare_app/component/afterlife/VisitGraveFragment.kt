package com.example.smartelderlycare_app.component.afterlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.smartelderlycare_app.R

class VisitGraveFragment : Fragment() {

    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_visit_grave, container, false)
        etDate = view.findViewById(R.id.etDate)
        etTime = view.findViewById(R.id.etTime)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        
        btnSubmit.setOnClickListener {
            val date = etDate.text.toString()
            val time = etTime.text.toString()
            // 这里可以添加预约逻辑
            // 例如显示预约成功的提示
        }
        
        return view
    }

    fun getVisitInfo(): Map<String, String> {
        return mapOf(
            "date" to etDate.text.toString(),
            "time" to etTime.text.toString()
        )
    }
}
