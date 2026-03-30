package com.example.smartelderlycare_app.component

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.smartelderlycare_app.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardCheckin = findViewById<CardView>(R.id.card_checkin)
        val cardCommunity = findViewById<CardView>(R.id.card_community)
        val cardAfterlife = findViewById<CardView>(R.id.card_afterlife)
        val cardStarrySky = findViewById<CardView>(R.id.card_starry_sky)
        val cardVoiceAssistant = findViewById<CardView>(R.id.card_voice_assistant)
        val cardMine = findViewById<CardView>(R.id.card_mine)

        cardCheckin.setOnClickListener {
            val intent = android.content.Intent(this, CheckinActivity::class.java)
            startActivity(intent)
        }
        cardCommunity.setOnClickListener {
            val intent = android.content.Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }

        cardVoiceAssistant.setOnClickListener {
            showToast("正在唤醒：语音助手...")
        }

        cardAfterlife.setOnClickListener {
            showToast("打开了：身后事定制")
        }

        cardStarrySky.setOnClickListener {
            showToast("打开了：星空纪念馆")
        }

        cardMine.setOnClickListener {
            showToast("打开了：我的中心")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}