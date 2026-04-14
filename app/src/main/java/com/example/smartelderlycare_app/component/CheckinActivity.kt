package com.example.smartelderlycare_app.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.smartelderlycare_app.R
import com.example.smartelderlycare_app.data.model.CheckinRecord
import com.example.smartelderlycare_app.data.repository.BmobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CheckinActivity : AppCompatActivity() {

    private lateinit var tvTotalPoints: TextView
    private lateinit var tvConsecutiveDays: TextView
    private lateinit var btnCheckin: MaterialCardView
    private lateinit var tvCheckinText: TextView
    private lateinit var progressBar: android.widget.ProgressBar

    private val repository = BmobRepository()
    private var myRecord: CheckinRecord? = null
    private var userId: String = ""
    private var nickname: String = ""
    private var avatarUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkin)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvTotalPoints = findViewById(R.id.tv_total_points)
        tvConsecutiveDays = findViewById(R.id.tv_consecutive_days)
        btnCheckin = findViewById(R.id.btn_checkin)
        tvCheckinText = findViewById(R.id.tv_checkin_text)
        progressBar = findViewById(R.id.progressBar)

        loadUserInfo()
        loadCheckinData()
    }

    private fun loadUserInfo() {
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        userId = prefs.getString("userObjectId", null)
            ?: prefs.getString("objectId", null) ?: ""
        nickname = prefs.getString("nickname", "") ?: ""
        avatarUrl = prefs.getString("avatarUrl", "") ?: ""
    }

    private fun loadCheckinData() {
        if (userId.isEmpty()) {
            showCenteredToast("请先登录")
            return
        }
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = repository.getCheckinRecord(userId)
                result.onSuccess { record ->
                    myRecord = record
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        updateUI()
                        loadLeaderboard()
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        updateUI()
                        loadLeaderboard()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    updateUI()
                    loadLeaderboard()
                }
            }
        }
    }

    private fun updateUI() {
        val record = myRecord
        if (record != null) {
            tvTotalPoints.text = "我的积分: ${record.totalPoints}"
            tvConsecutiveDays.text = "已连签: ${record.consecutiveDays}天"
        } else {
            tvTotalPoints.text = "我的积分: 0"
            tvConsecutiveDays.text = "已连签: 0天"
        }

        val today = getCurrentDate()
        val lastDate = record?.lastCheckinDate ?: ""
        if (lastDate == today) {
            btnCheckin.isClickable = false
            btnCheckin.isFocusable = false
            btnCheckin.setCardBackgroundColor(resources.getColor(R.color.gray, null))
            tvCheckinText.text = "已打卡"
        } else {
            btnCheckin.isClickable = true
            btnCheckin.isFocusable = true
            btnCheckin.setCardBackgroundColor(resources.getColor(R.color.green, null))
            tvCheckinText.text = "点击\n打卡"
        }

        btnCheckin.setOnClickListener {
            performCheckin()
        }
    }

    private fun performCheckin() {
        if (userId.isEmpty()) {
            showCenteredToast("请先登录")
            return
        }

        val today = getCurrentDate()
        val yesterday = getYesterdayDate()
        val record = myRecord

        if (record != null && record.lastCheckinDate == today) {
            showCenteredToast("今天已经打卡过了")
            return
        }

        val currentPoints = record?.totalPoints ?: 0
        val currentConsecutive = record?.consecutiveDays ?: 0
        val lastDate = record?.lastCheckinDate ?: ""

        val newPoints = currentPoints + 5
        val newConsecutive = if (lastDate == yesterday) currentConsecutive + 1 else 1
        val bonusPoints = if (newConsecutive % 7 == 0) 20 else 0
        val finalPoints = newPoints + bonusPoints

        val updatedRecord = CheckinRecord(
            objectId = record?.objectId,
            userId = userId,
            nickname = nickname,
            avatarUrl = avatarUrl,
            totalPoints = finalPoints,
            consecutiveDays = newConsecutive,
            lastCheckinDate = today
        )

        lifecycleScope.launch {
            try {
                if (record?.objectId != null) {
                    repository.updateCheckinRecord(record.objectId!!, updatedRecord)
                } else {
                    repository.createCheckinRecord(updatedRecord).onSuccess { saved ->
                        myRecord = saved
                    }
                }

                myRecord = updatedRecord
                withContext(Dispatchers.Main) {
                    updateUI()
                    loadLeaderboard()
                    if (bonusPoints > 0) {
                        showCenteredToast("🎉 连续打卡7天，额外奖励20分！")
                    } else {
                        showCenteredToast("打卡成功！获得5积分")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showCenteredToast("打卡失败，请重试")
                }
            }
        }
    }

    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                val result = repository.getLeaderboard(10)
                result.onSuccess { records ->
                    withContext(Dispatchers.Main) {
                        val rvLeaderboard = findViewById<RecyclerView>(R.id.rv_leaderboard)
                        rvLeaderboard.layoutManager = LinearLayoutManager(this@CheckinActivity)
                        rvLeaderboard.adapter = LeaderboardAdapter(records, userId)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun showCenteredToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(android.view.Gravity.CENTER, 0, 0)
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val text = layout.findViewById<TextView>(R.id.toast_text)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    class LeaderboardAdapter(
        private val userList: List<CheckinRecord>,
        private val currentUserId: String
    ) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRank: TextView = view.findViewById(R.id.tv_rank)
            val ivAvatar: ImageView = view.findViewById(R.id.iv_avatar)
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

            val displayName = if (user.userId == currentUserId) "我" else (user.nickname.ifEmpty { "用户" })
            holder.tvName.text = displayName
            holder.tvPoints.text = "${user.totalPoints} 分"

            if (!user.avatarUrl.isNullOrEmpty()) {
                val httpUrl = user.avatarUrl.replace("https://", "http://")
                Glide.with(holder.itemView.context)
                    .load(httpUrl)
                    .apply(RequestOptions()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop())
                    .into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
            }

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
