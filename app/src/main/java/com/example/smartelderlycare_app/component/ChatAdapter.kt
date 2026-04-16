package com.example.smartelderlycare_app.component

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartelderlycare_app.R
import java.util.*

class ChatAdapter(
    private val onSpeakClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_AI = 2

        fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    private val messages = mutableListOf<ChatMessage>()

    data class ChatMessage(
        val id: String = UUID.randomUUID().toString(),
        val content: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis(),
        val isComplete: Boolean = false
    )

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = inflater.inflate(R.layout.item_chat_user, parent, false)
                UserMessageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_chat_ai, parent, false)
                AiMessageViewHolder(view, onSpeakClick)
            }
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addUserMessage(content: String) {
        messages.add(ChatMessage(content = content, isUser = true))
        notifyItemInserted(messages.size - 1)
    }

    fun addAiMessage(content: String, isComplete: Boolean = false): String {
        val id = UUID.randomUUID().toString()
        messages.add(ChatMessage(id = id, content = content, isUser = false, isComplete = isComplete))
        notifyItemInserted(messages.size - 1)
        return id
    }

    fun updateAiMessage(messageId: String, newContent: String, isComplete: Boolean = false) {
        val index = messages.indexOfFirst { it.id == messageId }
        if (index >= 0) {
            messages[index] = messages[index].copy(content = newContent, isComplete = isComplete)
            notifyItemChanged(index)
        }
    }

    fun getLastAiMessageId(): String? {
        return messages.filter { !it.isUser }.lastOrNull()?.id
    }

    fun getMessages(): List<ChatMessage> = messages.toList()

    fun getMessageById(id: String): ChatMessage? {
        return messages.find { it.id == id }
    }

    fun getAllMessages(): List<ChatMessage> = messages.toList()

    fun setMessages(messageList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(messageList)
        notifyDataSetChanged()
    }

    abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(message: ChatMessage)
    }

    class UserMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        private val tvUserTime: TextView = itemView.findViewById(R.id.tvUserTime)

        override fun bind(message: ChatMessage) {
            tvUserMessage.text = message.content
            tvUserTime.text = formatTime(message.timestamp)
        }
    }

    class AiMessageViewHolder(
        itemView: View,
        private val onSpeakClick: ((String) -> Unit)?
    ) : MessageViewHolder(itemView) {
        private val tvAiMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
        private val tvAiTime: TextView = itemView.findViewById(R.id.tvAiTime)
        private val btnSpeak: ImageButton = itemView.findViewById(R.id.btnSpeak)
        private val layoutSpeak: LinearLayout = itemView.findViewById(R.id.layoutSpeak)

        override fun bind(message: ChatMessage) {
            tvAiMessage.text = message.content
            tvAiTime.text = formatTime(message.timestamp)

            layoutSpeak.visibility = if (message.isComplete) View.VISIBLE else View.GONE

            btnSpeak.setOnClickListener {
                onSpeakClick?.invoke(message.content)
            }
        }
    }
}