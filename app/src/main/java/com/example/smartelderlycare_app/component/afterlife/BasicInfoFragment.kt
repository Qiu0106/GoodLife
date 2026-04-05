package com.example.smartelderlycare_app.component.afterlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.smartelderlycare_app.R

class BasicInfoFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etContact: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_basic_info, container, false)
        etName = view.findViewById(R.id.etName)
        etAge = view.findViewById(R.id.etAge)
        etContact = view.findViewById(R.id.etContact)
        return view
    }

    fun getBasicInfo(): Map<String, String> {
        return mapOf(
            "name" to etName.text.toString(),
            "age" to etAge.text.toString(),
            "contact" to etContact.text.toString()
        )
    }
}
