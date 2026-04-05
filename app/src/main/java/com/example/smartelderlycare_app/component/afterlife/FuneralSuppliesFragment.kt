package com.example.smartelderlycare_app.component.afterlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.example.smartelderlycare_app.R

class FuneralSuppliesFragment : Fragment() {

    private lateinit var cbCoffin: CheckBox
    private lateinit var cbUrns: CheckBox
    private lateinit var cbFlowers: CheckBox
    private lateinit var cbCandles: CheckBox
    private lateinit var cbPhotos: CheckBox

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_funeral_supplies, container, false)
        cbCoffin = view.findViewById(R.id.cbCoffin)
        cbUrns = view.findViewById(R.id.cbUrns)
        cbFlowers = view.findViewById(R.id.cbFlowers)
        cbCandles = view.findViewById(R.id.cbCandles)
        cbPhotos = view.findViewById(R.id.cbPhotos)
        return view
    }

    fun getSelectedSupplies(): List<String> {
        val supplies = mutableListOf<String>()
        if (cbCoffin.isChecked) supplies.add("棺材")
        if (cbUrns.isChecked) supplies.add("骨灰盒")
        if (cbFlowers.isChecked) supplies.add("鲜花")
        if (cbCandles.isChecked) supplies.add("蜡烛")
        if (cbPhotos.isChecked) supplies.add("照片")
        return supplies
    }
}
