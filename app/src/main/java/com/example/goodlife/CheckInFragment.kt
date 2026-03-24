package com.example.goodlife

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CheckInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CheckInFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private data class LeaderEntry(
        val name: String,
        val score: Int
    )

    private fun todayString(): String {
        // yyyy-MM-dd：用于判断“今天”是否已打卡
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return fmt.format(Date())
    }

    private fun rewardLabel(streakDays: Int): String {
        return when {
            streakDays >= 30 -> "奖杯"
            streakDays >= 7 -> "勋章"
            streakDays >= 3 -> "小红花"
            else -> "暂无"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_check_in, container, false)

        val prefs: SharedPreferences =
            requireContext().getSharedPreferences("goodlife_checkin_prefs", Context.MODE_PRIVATE)

        val btnCheckIn: Button = view.findViewById(R.id.btn_check_in)
        val tvMyPoints: TextView = view.findViewById(R.id.tv_my_points)
        val tvStreakDays: TextView = view.findViewById(R.id.tv_streak_days)
        val tvRewardStatus: TextView = view.findViewById(R.id.tv_reward_status)

        fun setButtonForCheckedState(isChecked: Boolean) {
            if (isChecked) {
                // 已打卡时仅视觉变灰，但仍可点击以弹出“您今日已打卡！”提示
                btnCheckIn.isEnabled = true
                btnCheckIn.text = "已打卡"
                btnCheckIn.setBackgroundColor(Color.parseColor("#BDBDBD"))
                btnCheckIn.setTextColor(Color.parseColor("#666666"))
            } else {
                btnCheckIn.isEnabled = true
                btnCheckIn.text = "请打卡"
                btnCheckIn.setBackgroundColor(Color.parseColor("#F2C94C"))
                btnCheckIn.setTextColor(Color.parseColor("#000000"))
            }
        }

        fun showCheckInDialog(message: String) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_checkin, null)
            val tvMessage: TextView = dialogView.findViewById(R.id.tv_dialog_message)
            val btnClose: ImageButton = dialogView.findViewById(R.id.btn_dialog_close)
            tvMessage.text = message

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnClose.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        fun updateLeaderboardDisplay(currentMyPoints: Int) {
            tvMyPoints.text = "用户积分：$currentMyPoints"

            val others = listOf(
                LeaderEntry(name = "张老师", score = 3),
                LeaderEntry(name = "李阿姨", score = 5),
                LeaderEntry(name = "王叔叔", score = 2),
                LeaderEntry(name = "赵阿姨", score = 4)
            )

            val all = (others + LeaderEntry(name = "我", score = currentMyPoints))
                .sortedByDescending { it.score }
                .take(5)

            val rankUsers = listOf(
                view.findViewById<TextView>(R.id.tv_rank1_user),
                view.findViewById<TextView>(R.id.tv_rank2_user),
                view.findViewById<TextView>(R.id.tv_rank3_user),
                view.findViewById<TextView>(R.id.tv_rank4_user),
                view.findViewById<TextView>(R.id.tv_rank5_user)
            )
            val rankScores = listOf(
                view.findViewById<TextView>(R.id.tv_rank1_score),
                view.findViewById<TextView>(R.id.tv_rank2_score),
                view.findViewById<TextView>(R.id.tv_rank3_score),
                view.findViewById<TextView>(R.id.tv_rank4_score),
                view.findViewById<TextView>(R.id.tv_rank5_score)
            )

            for (i in 0 until 5) {
                val entry = all.getOrNull(i) ?: LeaderEntry(name = "-", score = 0)
                rankUsers[i].text = entry.name
                rankScores[i].text = entry.score.toString()
            }
        }

        val today = todayString()
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        // 用于判断：上一次打卡是否在“昨天”
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = fmt.format(cal.time)

        val checkedToday = prefs.getString("last_checkin_date", "") == today
        val myPoints = prefs.getInt("points", 0)
        val myStreakDays = prefs.getInt("streak_days", 0)

        setButtonForCheckedState(checkedToday)
        updateLeaderboardDisplay(myPoints)
        tvStreakDays.text = "连续签到：${myStreakDays}天"
        tvRewardStatus.text = "当前奖励：${rewardLabel(myStreakDays)}"

        btnCheckIn.setOnClickListener {
            // 点击时再判断一次，保证状态正确
            val nowCheckedToday = prefs.getString("last_checkin_date", "") == today
            if (nowCheckedToday) {
                val currentStreak = prefs.getInt("streak_days", 0)
                showCheckInDialog("您今日已打卡！已连续签到${currentStreak}天")
                return@setOnClickListener
            }

            // 今日未打卡：打卡成功，积分加 1
            val lastDate = prefs.getString("last_checkin_date", "") ?: ""
            val oldStreakDays = prefs.getInt("streak_days", 0)

            val newStreakDays = if (lastDate == yesterday) oldStreakDays + 1 else 1

            val newPoints = prefs.getInt("points", 0) + 1
            prefs.edit()
                .putString("last_checkin_date", today)
                .putInt("points", newPoints)
                .putInt("streak_days", newStreakDays)
                .apply()

            setButtonForCheckedState(true)
            updateLeaderboardDisplay(newPoints)
            tvStreakDays.text = "连续签到：${newStreakDays}天"
            tvRewardStatus.text = "当前奖励：${rewardLabel(newStreakDays)}"

            val rewardMessage = when (newStreakDays) {
                3 -> "恭喜您打卡成功！连续签到3天奖励一朵小红花！"
                7 -> "恭喜您打卡成功！连续签到7天奖励一枚勋章！"
                30 -> "恭喜您打卡成功！连续签到30天奖励奖杯！"
                else -> "恭喜您打卡成功！已连续签到${newStreakDays}天！"
            }
            showCheckInDialog(rewardMessage)
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CheckInFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CheckInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}