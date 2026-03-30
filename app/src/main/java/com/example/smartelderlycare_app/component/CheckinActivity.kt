package com.example.smartelderlycare_app.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import java.text.SimpleDateFormat
import java.util.*

class CheckinActivity : AppCompatActivity() {

    private lateinit var tvTotalPoints: TextView
    private lateinit var tvConsecutiveDays: TextView
    private lateinit var btnCheckin: CardView
    private lateinit var tvCheckinText: TextView
    
    private var totalPoints = 0
    private var consecutiveDays = 0
    private var lastCheckinDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkin)

        tvTotalPoints = findViewById(R.id.tv_total_points)
        tvConsecutiveDays = findViewById(R.id.tv_consecutive_days)
        btnCheckin = findViewById(R.id.btn_checkin)
        tvCheckinText = findViewById(R.id.tv_checkin_text)

        loadCheckinData()
        updateUI()
        setupCheckinButton()
        setupDynamicLeaderboard()
    }

    private fun loadCheckinData() {
        val sharedPreferences = getSharedPreferences("checkin_data", MODE_PRIVATE)
        totalPoints = sharedPreferences.getInt("total_points", 0)
        consecutiveDays = sharedPreferences.getInt("consecutive_days", 0)
        lastCheckinDate = sharedPreferences.getString("last_checkin_date", "") ?: ""
    }

    private fun saveCheckinData() {
        val sharedPreferences = getSharedPreferences("checkin_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("total_points", totalPoints)
        editor.putInt("consecutive_days", consecutiveDays)
        editor.putString("last_checkin_date", lastCheckinDate)
        editor.apply()
    }

    private fun updateUI() {
        tvTotalPoints.text = "我的积分: $totalPoints"
        tvConsecutiveDays.text = "已连签: ${consecutiveDays}天"
        
        val today = getCurrentDate()
        if (lastCheckinDate == today) {
            btnCheckin.isClickable = false
            btnCheckin.isFocusable = false
            btnCheckin.setCardBackgroundColor(resources.getColor(R.color.gray))
            tvCheckinText.text = "已打卡"
        } else {
            btnCheckin.isClickable = true
            btnCheckin.isFocusable = true
            btnCheckin.setCardBackgroundColor(resources.getColor(R.color.green))
            tvCheckinText.text = "点击\n打卡"
        }
    }

    private fun setupCheckinButton() {
        btnCheckin.setOnClickListener {
            performCheckin()
        }
    }

    private fun performCheckin() {
        val today = getCurrentDate()
        val yesterday = getYesterdayDate()
        
        if (lastCheckinDate == today) {
            showCenteredToast("今天已经打卡过了")
            return
        }
        
        // 增加积分（每天5分）
        totalPoints += 5
        
        // 更新连续打卡天数
        if (lastCheckinDate == yesterday) {
            consecutiveDays += 1
        } else {
            consecutiveDays = 1
        }
        
        // 连续打卡奖励
        if (consecutiveDays % 7 == 0) {
            totalPoints += 20 // 连续7天额外奖励20分
            showCenteredToast("🎉 连续打卡7天，额外奖励20分！")
        }
        
        // 更新最后打卡日期
        lastCheckinDate = today
        
        // 保存数据
        saveCheckinData()
        
        // 更新UI
        updateUI()
        
        // 显示打卡成功提示
        showCenteredToast("打卡成功！获得5积分")
        
        // 更新排行榜数据
        setupDynamicLeaderboard()
    }

    private fun showCenteredToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(android.view.Gravity.CENTER, 0, 0)
        
        // 创建自定义Toast布局
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val text = layout.findViewById<android.widget.TextView>(R.id.toast_text)
        text.text = message
        
        toast.view = layout
        toast.show()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun setupDynamicLeaderboard() {
        val rvLeaderboard = findViewById<RecyclerView>(R.id.rv_leaderboard)

        // 生成动态排行榜数据
        val users = generateLeaderboardData()

        rvLeaderboard.layoutManager = LinearLayoutManager(this)
        rvLeaderboard.adapter = LeaderboardAdapter(users)
    }

    private fun generateLeaderboardData(): List<UserRank> {
        val userNames = listOf("李建国", "王世清", "赵秀梅", "刘斌", "陈秀花", "张栋", "王亭枝", "李裕民", "赵丽华")
        val users = mutableListOf<UserRank>()

        // 生成其他用户的随机积分（100-1000分）
        for (name in userNames) {
            val randomPoints = (100..1000).random()
            users.add(UserRank(name, randomPoints))
        }

        // 添加自己的积分
        users.add(UserRank("我 ", totalPoints))

        // 按积分降序排序
        users.sortByDescending { it.points }

        // 只取前10名
        return users.take(10)
    }

    data class UserRank(val name: String, val points: Int)

    class LeaderboardAdapter(private val userList: List<UserRank>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRank: TextView = view.findViewById(R.id.tv_rank)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvPoints: TextView = view.findViewById(R.id.tv_points)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = userList[position]
            holder.tvRank.text = (position + 1).toString()
            holder.tvName.text = user.name
            holder.tvPoints.text = "${user.points} 分"

            when (position) {
                0 -> holder.tvRank.setTextColor(android.graphics.Color.parseColor("#FFD700"))
                1 -> holder.tvRank.setTextColor(android.graphics.Color.parseColor("#C0C0C0"))
                2 -> holder.tvRank.setTextColor(android.graphics.Color.parseColor("#CD7F32"))
                else -> holder.tvRank.setTextColor(android.graphics.Color.parseColor("#757575"))
            }
        }

        override fun getItemCount() = userList.size
    }
}