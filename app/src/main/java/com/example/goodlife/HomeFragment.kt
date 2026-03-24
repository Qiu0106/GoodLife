package com.example.goodlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载主页布局
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 找到四个按钮
        val btnCheckIn: Button = view.findViewById(R.id.btn_check_in)
        val btnCommunity: Button = view.findViewById(R.id.btn_community)
        val btnStar: Button = view.findViewById(R.id.btn_star)
        val btnProfile: Button = view.findViewById(R.id.btn_profile)

        // 为“打卡”按钮设置点击事件，跳转到 CheckInFragment
        btnCheckIn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CheckInFragment())
                .addToBackStack(null)
                .commit()
        }

        // 为“社区”按钮设置点击事件，跳转到 CommunityFragment
        btnCommunity.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CommunityFragment())
                .addToBackStack(null)
                .commit()
        }

        // 为“星空”按钮设置点击事件，跳转到 StarFragment（稍后创建）
        btnStar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StarFragment())
                .addToBackStack(null)
                .commit()
        }

        // 为“我的”按钮设置点击事件，跳转到 ProfileFragment
        btnProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    companion object {
        // 后面如果需要通过 newInstance 传参数，可以在这里再补充
    }
}