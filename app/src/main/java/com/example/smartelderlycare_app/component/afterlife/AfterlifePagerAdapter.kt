package com.example.smartelderlycare_app.component.afterlife

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AfterlifePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = listOf(
        BasicInfoFragment(),
        FuneralStyleFragment(),
        RelicsBurialFragment(),
        FuneralSuppliesFragment(),
        BGMFragment(),
        VisitGraveFragment()
    )

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun getFragment(position: Int): Fragment {
        return fragments[position]
    }
}
