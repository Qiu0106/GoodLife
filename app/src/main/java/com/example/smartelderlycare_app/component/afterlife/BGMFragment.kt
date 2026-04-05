package com.example.smartelderlycare_app.component.afterlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.smartelderlycare_app.R

class BGMFragment : Fragment() {

    private lateinit var rgBGM: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bgm, container, false)
        rgBGM = view.findViewById(R.id.rgBGM)
        return view
    }

    fun getSelectedBGM(): String {
        val selectedId = rgBGM.checkedRadioButtonId
        val radioButton = view?.findViewById<RadioButton>(selectedId)
        return radioButton?.text.toString()
    }
}
