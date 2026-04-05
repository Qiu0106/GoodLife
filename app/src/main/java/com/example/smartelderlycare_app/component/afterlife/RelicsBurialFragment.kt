package com.example.smartelderlycare_app.component.afterlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.smartelderlycare_app.R

class RelicsBurialFragment : Fragment() {

    private lateinit var rgRelics: RadioGroup
    private lateinit var rgBurial: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_relics_burial, container, false)
        rgRelics = view.findViewById(R.id.rgRelics)
        rgBurial = view.findViewById(R.id.rgBurial)
        return view
    }

    fun getRelicsHandling(): String {
        val selectedId = rgRelics.checkedRadioButtonId
        val radioButton = view?.findViewById<RadioButton>(selectedId)
        return radioButton?.text.toString()
    }

    fun getBurialMethod(): String {
        val selectedId = rgBurial.checkedRadioButtonId
        val radioButton = view?.findViewById<RadioButton>(selectedId)
        return radioButton?.text.toString()
    }
}
