package com.example.smartelderlycare_app.component.afterlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.smartelderlycare_app.R

class FuneralStyleFragment : Fragment() {

    private lateinit var rgFuneralStyle: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_funeral_style, container, false)
        rgFuneralStyle = view.findViewById(R.id.rgFuneralStyle)
        return view
    }

    fun getFuneralStyle(): String {
        val selectedId = rgFuneralStyle.checkedRadioButtonId
        if (selectedId == -1) return ""
        val radioButton = rgFuneralStyle.findViewById<RadioButton>(selectedId)
        return radioButton?.text?.toString() ?: ""
    }
}
