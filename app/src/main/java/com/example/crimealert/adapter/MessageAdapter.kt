package com.example.crimealert.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.crimealert.databinding.MessageItemBinding
import com.example.crimealert.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(private val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.senderTextView.text = message.name
            binding.messageTextView.visibility = if (message.text != null) {
                binding.messageTextView.text = message.text
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.dateTextView.text = formatDate(message.timestamp)
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val date = Date(timestamp)
            return sdf.format(date)
        }
    }
}
